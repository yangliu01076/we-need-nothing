import java.io.Serializable;

/**
 * @Describe
 * @Date 2025/5/19
 * @Author duoyian
 */
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    protected boolean success;
    protected Integer errorCode;
    protected String errorMsg;
    protected T result;

    public BaseResponse() {
    }

    private BaseResponse(Builder<T> builder) {
        this.success = builder.success;
        this.errorCode = builder.errorCode;
        this.errorMsg = builder.errorMsg;
        this.result = builder.result;
    }

    public static <T> Builder<T> newSuccResponse() {
        return new Builder<T>().success(true);
    }

    public static <T> Builder<T> newFailResponse() {
        return new Builder<T>().success(false);
    }

    public static final class Builder<T> {
        private boolean success = false;
        private Integer errorCode;
        private String errorMsg;
        private T result;

        private Builder() {
        }

        public BaseResponse<T> build() {
            return new BaseResponse<T>(this);
        }

        public Builder<T> success(boolean success) {
            this.success = success;
            this.errorCode = 0;
            return this;
        }

        public Builder<T> errorCode(Integer errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder<T> errorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
            return this;
        }

        public Builder<T> result(T result) {
            this.result = result;
            return this;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public T getResult() {
        return result;
    }
}
