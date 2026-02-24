import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String s = "serviceA:sayHello:xx";
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        System.out.println(bytes.length);
        System.out.println(Arrays.toString(bytes));
        System.out.println(new String(bytes, StandardCharsets.UTF_8));
    }

    private void test(Integer a) {
        if (1 == a) {
            System.out.println("a = " + a);


        }
    }

    public static void kp(int[] arr, int low, int high) {
        if (low >= high) {
            return;
        }
        int part = part(arr, low, high);
        kp(arr, low, part - 1);
        kp(arr, part + 1, high);
    }

    public static int part(int[] arr, int low, int high) {
        int t = arr[low];
        int i = low;
        int j = high;
        while (i < j) {
            while (i < j && arr[j] >= t) {
                j--;
            }
            arr[i] = arr[j];
            while (i < j && arr[i] <= t) {
                i++;
            }
            arr[j] = arr[i];
        }
        arr[i] = t;
        return i;
    }

    private boolean isTrue(int num) {
        if (num < 0) {
            return false;
        }
        if (num == 0) {
            return true;
        }
        int[] bits = getBits(num);
        int i = 0, j = bits.length - 1;
        while (i < j && bits[i] == 0) {
            i++;
        }
        while (i < j) {
            if (bits[j] == bits[i]) {
                i++;
                j--;
            } else {
                return false;
            }
        }
        return true;
    }

    private int[] getBits(int num) {
        int[] bits = new int[32];
        if (num <= 0) {
            return bits;
        }
        int index = 31;
        while (num != 0) {
            bits[index--] = num & 1;
            num >>= 1;
        }
        return bits;
    }

    private void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pivot = partition(arr, low, high);
            quickSort(arr, low, pivot - 1);
            quickSort(arr, pivot + 1, high);
        }
    }

    private int partition(int[] arr, int low, int high) {
        int pivot = arr[low];
        int i = low;
        int j = high;
        while (i < j) {
            while (i < j && arr[j] >= pivot) {
                j--;
            }
            arr[i] = arr[j];
            while (i < j && arr[i] <= pivot) {
                i++;
            }
            arr[j] = arr[i];
        }
        arr[i] = pivot;
        return i;
    }

    public enum BdApplyStatusEnum {

        FILL_INFO_GET_CODE(1, "网点填写入驻信息并获取公司码"),
        SCAN_SUBMIT_QUALIFICATION(2, "扫码登录小程序提交资质审核"),
        WAIT_QUALIFICATION_APPROVAL(3, "等待运营完成资质审核"),
        PUSH_AGREEMENT_PENDING(4, "推送协议等待审批"),
        SIGN_AGREEMENT_COMPLETE(5, "网点进入小程序完成签署协议"),
        ;

        public Integer getCode(BdApplyStatusEnum statusEnum) {
            return statusEnum.code;
        }


        private final Integer code;

        private final String desc;

        BdApplyStatusEnum(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static List<BdApplyStatusEnum> getStatusEnumListFromBeginToCode(Integer code) {
            BdApplyStatusEnum[] values = BdApplyStatusEnum.values();
            return new ArrayList<>(Arrays.asList(values).subList(0, code));
        }
    }
}