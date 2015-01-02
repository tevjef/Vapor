package deadpixel.app.vapor.cloudapp.impl.model;

/**
 * Created by Tevin on 6/10/2014.
 */
public class UploadResponseModel {

    Long uploads_remaining;
    Long max_upload_size;
    String url;
    Parameters params;

    public Long getUploads_remaining() {
        return uploads_remaining;
    }

    public void setUploads_remaining(long uploads_remaining) {
        this.uploads_remaining = uploads_remaining;
    }

    public Long getMax_upload_size() {
        return max_upload_size;
    }

    public void setMax_upload_size(long max_upload_size) {
        this.max_upload_size = max_upload_size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Parameters getParams() {
        return params;
    }

    public void setParams(Parameters params) {
        this.params = params;
    }

    public class Parameters {

        String AWSAccessKeyId;
        String key;
        String acl;
        String success_action_redirect;
        String signature;
        String policy;

        public String getAWSAccessKeyId() {
            return AWSAccessKeyId;
        }

        public void setAWSAccessKeyId(String AWSAccessKeyId) {
            this.AWSAccessKeyId = AWSAccessKeyId;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getAcl() {
            return acl;
        }

        public void setAcl(String acl) {
            this.acl = acl;
        }

        public String getSuccess_action_redirect() {
            return success_action_redirect;
        }

        public void setSuccess_action_redirect(String success_action_redirect) {
            this.success_action_redirect = success_action_redirect;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }
    }
}
