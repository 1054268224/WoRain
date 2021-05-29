package com.example.systemmanageruidemo.powersavemanager.adpter;

public class PowerItem {

    public static class Detail {
        private String title;
        private String desc;

        public Detail(String title, String desc) {
            this.title = title;
            this.desc = desc;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


    public static class Mode{
        private String title;
        private String description;
        private String detail;
        private Boolean isTrue;

        public Mode(String title, String description, String detail, Boolean isTrue) {
            this.title = title;
            this.description = description;
            this.detail = detail;
            this.isTrue = isTrue;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public Boolean getTrue() {
            return isTrue;
        }

        public void setTrue(Boolean aTrue) {
            isTrue = aTrue;
        }

    }
}
