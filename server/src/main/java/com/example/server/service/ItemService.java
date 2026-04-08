package com.example.server.service;

import com.example.server.dto.ItemCreateRequest;
import com.example.server.dto.ItemVO;
import com.example.server.dto.PageResponse;
import com.example.server.entity.Item;
import com.example.server.entity.ItemCategory;
import com.example.server.entity.User;
import com.example.server.exception.BusinessException;
import com.example.server.repository.ItemCategoryRepository;
import com.example.server.repository.ItemCommentRepository;
import com.example.server.repository.ItemRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;  //数据库操作接口
    private final ItemCategoryRepository categoryRepository;  //数据库操作接口
    private final ItemCommentRepository commentRepository; //数据库操作接口
    private final UserService userService;   //处理用户信息

    private static final int TYPE_LOST = 0;   //丢失
    private static final int TYPE_FOUND = 1;  //招领
    private static final int STATUS_SEARCHING = 0;  //寻找中
    private static final int STATUS_FOUND = 1;  //已找到
    private static final int STATUS_CANCELLED = 2;  //取消
    private static final int STATUS_EXPIRED = 3; //过期

    // 轻量同义词词典：最小实现，优先覆盖常见失物词
    private static final Map<String, List<String>> QUERY_SYNONYM_MAP = new HashMap<>();

    static {
        addSynonymGroup("书", "书籍", "课本", "教材", "笔记本", "讲义");
        addSynonymGroup("手机", "电话", "苹果手机", "安卓手机");
        addSynonymGroup("电脑", "笔记本电脑", "笔记本", "平板", "ipad");
        addSynonymGroup("证件", "身份证", "学生证", "校园卡", "银行卡", "卡");
        addSynonymGroup("钥匙", "钥匙扣", "钥匙串", "车钥匙");
        addSynonymGroup("水杯", "杯子", "保温杯", "水壶");
        addSynonymGroup("雨伞", "伞");
        addSynonymGroup("背包", "书包", "包", "双肩包", "挎包");
    }

    private static void addSynonymGroup(String... words) {
        List<String> group = Arrays.stream(words).map(String::toLowerCase).distinct().toList();
        for (String w : group) {
            QUERY_SYNONYM_MAP.put(w, group);
        }
    }

    //创建物品信息
    @Transactional
    public ItemVO create(Long userId, ItemCreateRequest req) {
        userService.assertNotBanned(userId);   //未被封禁才可以发布
        User user = userService.findById(userId); //查用户身份
        ItemCategory category = categoryRepository.findById(req.getCategoryId())   //查分类是否对
                .orElseThrow(() -> new BusinessException(400, "分类不存在"));
        //没问题就创建物品信息
        Item item = Item.builder()
                .user(user)
                .category(category)
                .type(req.getType())  //失物or招领
                .title(req.getTitle())
                .description(req.getDescription())
                .location(req.getLocation())
                .images(req.getImages())
                .contact(req.getContact())
                .status(STATUS_SEARCHING)
                .eventTime(req.getEventTime())
                .build();
        item = itemRepository.save(item);
        return toItemVO(item, 0);
    }

    //查询筛选功能  根据传的信息筛选
    //keyword：输入的关键字 type：0丢失 1招领  categoryId：分类id status状态
    public PageResponse<ItemVO> list(String keyword, Long categoryId, Integer type, Integer status, int page, int size) {
        List<String> expandedKeywords = expandKeywords(keyword);
        Specification<Item> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (status != null) { //传了状态就按状态查，没有就默认只差寻找中的
                preds.add(cb.equal(root.get("status"), status));
            } else {
                preds.add(cb.equal(root.get("status"), STATUS_SEARCHING));
            }
            if (type != null) preds.add(cb.equal(root.get("type"), type));
            if (categoryId != null) preds.add(cb.equal(root.get("category").get("id"), categoryId));
            //关键词模糊匹配
            if (!expandedKeywords.isEmpty()) {
                List<Predicate> keywordPreds = new ArrayList<>();
                for (String kw : expandedKeywords) {
                    String k = "%" + kw + "%";
                    keywordPreds.add(cb.like(cb.lower(root.get("title")), k));
                    keywordPreds.add(cb.like(cb.lower(root.get("description")), k));
                    keywordPreds.add(cb.like(cb.lower(root.get("location")), k));
                }
                preds.add(cb.or(keywordPreds.toArray(new Predicate[0])));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> p = itemRepository.findAll(spec, pageable);
        List<Item> rankedItems = rerankItems(p.getContent(), keyword, expandedKeywords);
        List<ItemVO> content = rankedItems.stream()
                .map(i -> toItemVO(i, commentRepository.findByItemIdOrderByCreatedAtAsc(i.getId()).size()))
                .collect(Collectors.toList());
        return PageResponse.<ItemVO>builder()
                .content(content)
                .total(p.getTotalElements())
                .page(p.getNumber())
                .size(p.getSize())
                .totalPages(p.getTotalPages())
                .build();
    }

    //查询，根据item主键id ，返回物品信息（管理/内部用，不隐藏已过期）
    public ItemVO getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "物品不存在"));
        int cc = commentRepository.findByItemIdOrderByCreatedAtAsc(item.getId()).size();
        return toItemVO(item, cc);
    }

    //前台查看详情：已过期(status=3)仅发布者本人可见，其他人视为不存在
    public ItemVO getByIdForViewer(Long id, Long viewerUserId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "物品不存在"));
        if (item.getStatus() == STATUS_EXPIRED) {
            if (viewerUserId == null || !item.getUser().getId().equals(viewerUserId)) {
                throw new BusinessException(404, "物品不存在或已下架");
            }
        }
        int cc = commentRepository.findByItemIdOrderByCreatedAtAsc(item.getId()).size();
        return toItemVO(item, cc);
    }

    //修改物品状态
    @Transactional
    public ItemVO updateStatus(Long itemId, Long userId, Integer newStatus) {
        userService.assertNotBanned(userId);  //账号封禁不允许操作
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "物品不存在"));
        if (!item.getUser().getId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        item.setStatus(newStatus);
        item = itemRepository.save(item);
        return toItemVO(item, commentRepository.findByItemIdOrderByCreatedAtAsc(item.getId()).size());
    }

    //获取我的物品信息 通过type =0 丢失 =1 招领 type为item表中项
    public List<ItemVO> listMyItems(Long userId, int type) {
        List<Item> items = itemRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
        return items.stream()
                .map(i -> toItemVO(i, commentRepository.findByItemIdOrderByCreatedAtAsc(i.getId()).size()))
                .collect(Collectors.toList());
    }

    /** 管理端列表：不传 status 时查全部，不默认只查寻找中 */
    public PageResponse<ItemVO> adminList(String keyword, Long categoryId, Integer type, Integer status, int page, int size) {
        List<String> expandedKeywords = expandKeywords(keyword);
        Specification<Item> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (status != null) preds.add(cb.equal(root.get("status"), status));
            if (type != null) preds.add(cb.equal(root.get("type"), type));
            if (categoryId != null) preds.add(cb.equal(root.get("category").get("id"), categoryId));
            if (!expandedKeywords.isEmpty()) {
                List<Predicate> keywordPreds = new ArrayList<>();
                for (String kw : expandedKeywords) {
                    String k = "%" + kw + "%";
                    keywordPreds.add(cb.like(cb.lower(root.get("title")), k));
                    keywordPreds.add(cb.like(cb.lower(root.get("description")), k));
                    keywordPreds.add(cb.like(cb.lower(root.get("location")), k));
                }
                preds.add(cb.or(keywordPreds.toArray(new Predicate[0])));
            }
            return cb.and(preds.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> p = itemRepository.findAll(spec, pageable);
        List<Item> rankedItems = rerankItems(p.getContent(), keyword, expandedKeywords);
        List<ItemVO> content = rankedItems.stream()
                .map(i -> toItemVO(i, commentRepository.findByItemIdOrderByCreatedAtAsc(i.getId()).size()))
                .collect(Collectors.toList());
        return PageResponse.<ItemVO>builder()
                .content(content)
                .total(p.getTotalElements())
                .page(p.getNumber())
                .size(p.getSize())
                .totalPages(p.getTotalPages())
                .build();
    }

    //将输入的关键字拓展成同义词，加大查询范围
    private List<String> expandKeywords(String keyword) {
        if (!StringUtils.hasText(keyword)) return List.of();   //keyword为null返回空列表
        String k = keyword.trim().toLowerCase();   //去空格转小写
        LinkedHashSet<String> expanded = new LinkedHashSet<>();  //结果去重 + 保序
        expanded.add(k);
        for (Map.Entry<String, List<String>> e : QUERY_SYNONYM_MAP.entrySet()) {
            String key = e.getKey();
            if (k.contains(key) || key.contains(k)) {
                expanded.addAll(e.getValue());
            }
        }
        return expanded.stream().limit(8).toList();
    }

    private List<Item> rerankItems(List<Item> items, String keyword, List<String> expandedKeywords) {
        if (!StringUtils.hasText(keyword) || items.isEmpty()) return items;
        return items.stream()
                .sorted(Comparator
                        .comparingDouble((Item i) -> scoreItem(i, keyword, expandedKeywords)).reversed()
                        .thenComparing(Item::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /**
     * Hybrid最小实现：
     * 1) 原词命中加分 2) 同义词命中加分 3) 字符2-gram Jaccard相似度加分
     */
    private double scoreItem(Item item, String keyword, List<String> expandedKeywords) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        String title = item.getTitle() == null ? "" : item.getTitle().toLowerCase();
        String desc = item.getDescription() == null ? "" : item.getDescription().toLowerCase();
        String location = item.getLocation() == null ? "" : item.getLocation().toLowerCase();
        String allText = (title + " " + desc + " " + location).trim();

        double score = 0d;
        if (!k.isEmpty() && allText.contains(k)) score += 5d;
        for (String syn : expandedKeywords) {
            if (allText.contains(syn)) score += 1.5d;
        }

        double titleSim = ngramJaccard(k, title, 2);
        double allSim = ngramJaccard(k, allText, 2);
        score += (titleSim * 2.5d + allSim * 1.5d);
        return score;
    }

    private double ngramJaccard(String a, String b, int n) {
        if (!StringUtils.hasText(a) || !StringUtils.hasText(b)) return 0d;
        Set<String> aSet = toNgrams(a, n);
        Set<String> bSet = toNgrams(b, n);
        if (aSet.isEmpty() || bSet.isEmpty()) return 0d;
        Set<String> intersection = new HashSet<>(aSet);
        intersection.retainAll(bSet);
        Set<String> union = new HashSet<>(aSet);
        union.addAll(bSet);
        if (union.isEmpty()) return 0d;
        return (double) intersection.size() / union.size();
    }

    private Set<String> toNgrams(String text, int n) {
        Set<String> set = new HashSet<>();
        String s = text.trim();
        if (s.isEmpty()) return set;
        if (s.length() <= n) {
            set.add(s);
            return set;
        }
        for (int i = 0; i <= s.length() - n; i++) {
            set.add(s.substring(i, i + n));
        }
        return set;
    }

    // 管理员编辑物品内容
    @Transactional(readOnly = false)
    public ItemVO adminUpdate(Long itemId, String title, String description, String location, String contact) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "物品不存在"));
        if (title != null) item.setTitle(title);
        if (description != null) item.setDescription(description);
        if (location != null) item.setLocation(location);
        if (contact != null) item.setContact(contact);
        item = itemRepository.save(item);
        return toItemVO(item, commentRepository.findByItemIdOrderByCreatedAtAsc(item.getId()).size());
    }

    //管理员修改物品状态（含恢复过期等）
    @Transactional(readOnly = false)
    public ItemVO adminUpdateStatus(Long itemId, Integer newStatus) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "物品不存在"));
        item.setStatus(newStatus);
        item = itemRepository.save(item);
        return toItemVO(item, commentRepository.findByItemIdOrderByCreatedAtAsc(item.getId()).size());
    }

    //删除物品信息，管理员或本人可删
    @Transactional
    public void delete(Long itemId, Long operatorId, boolean isAdmin) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(404, "物品不存在"));
        if (!isAdmin && !item.getUser().getId().equals(operatorId)) {
            throw new BusinessException(403, "无权操作");
        }
        itemRepository.delete(item);
        itemRepository.delete(item);
    }

    //将物品信息item封装成前端要用的数据对象itemVo
    private ItemVO toItemVO(Item item, int commentCount) {
        return ItemVO.builder()
                .id(item.getId())
                .userId(item.getUser().getId())
                .userNickname(item.getUser().getNickname())
                .userAvatarUrl(item.getUser().getAvatarUrl())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .type(item.getType())
                .title(item.getTitle())
                .description(item.getDescription())
                .location(item.getLocation())
                .images(item.getImages())
                .contact(item.getContact())
                .status(item.getStatus())
                .eventTime(item.getEventTime())
                .createdAt(item.getCreatedAt())
                .commentCount(commentCount)
                .build();
    }
}
