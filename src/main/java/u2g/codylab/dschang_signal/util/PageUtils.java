package u2g.codylab.dschang_signal.util;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;

public class PageUtils {

    private PageUtils() {
        // Utility class
    }

    public static Pageable buildPageable(Integer page, Integer size, String sort) {
        Sort.Order order;
        String[] sortSplit = sort.split(",");
        String valueField = sortSplit[0];
        String sortingField = sortSplit[1];

        order = new Sort.Order(Sort.Direction.fromString(sortingField), valueField);

        return PageRequest.of(page, size, Sort.by(order));
    }

    public static String checkSort(String sort, List<String> sortingFields) {
        String[] split = sort.split(",");

        if (!sortingFields.contains(split[0])) {
            throw new RuntimeException("Invalid sort field");
        }

        if (split.length > 2) {
            throw new RuntimeException("Sort field has too many parts");
        }

        if (split.length == 1) {
            split = new String[]{split[0], "desc"};
        } else if (!("asc".equalsIgnoreCase(split[1]) || "desc".equalsIgnoreCase(split[1]))) {
            throw new RuntimeException("Invalid sort direction");
        }

        return "%s,%s".formatted(split[0], split[1]);
    }

    public static <T> HttpHeaders generatePaginationHttpHeaders(Page<T> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", Long.toString(page.getTotalElements()));
        headers.add("X-Page-Size", Long.toString(page.getNumberOfElements()));
        headers.add("X-Page-Number", Long.toString(page.getNumber()));
        return headers;
    }
}
