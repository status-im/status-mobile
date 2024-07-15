(ns status-im.contexts.wallet.common.activity-tab.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.shell.jump-to.constants :as jump-to.constants]
    [status-im.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn send-and-receive-activity
  [{:keys [transaction relative-date status sender recipient token amount network-name
           network-logo]}]
  [quo/wallet-activity
   {:transaction       transaction
    :timestamp         relative-date
    :status            status
    :counter           1
    :first-tag         {:size   24
                        :type   :token
                        :token  token
                        :amount amount}
    :second-tag-prefix :t/from
    :second-tag        {:type :address :address sender}
    :third-tag-prefix  :t/to
    :third-tag         {:type :address :address recipient}
    :fourth-tag-prefix :t/via
    :fourth-tag        {:size         24
                        :type         :network
                        :network-name network-name
                        :network-logo network-logo}
    :blur?             false}])

(defn activity-item
  [{:keys [transaction] :as activity}]
  (case transaction
    (:send :receive) [send-and-receive-activity activity]
    nil))

(defn view
  []
  (let [theme         (quo.theme/use-theme)
        activity-list (rf/sub [:wallet/activities-for-current-viewing-account])]
    (if (empty? activity-list)
      [empty-tab/view
       {:title       (i18n/label :t/no-activity)
        :description (i18n/label :t/empty-tab-description)
        :image       (resources/get-themed-image :no-activity theme)}]
      [rn/section-list
       {:sections                       activity-list
        :sticky-section-headers-enabled false
        :style                          {:flex               1
                                         :padding-horizontal 8}
        :content-container-style        {:padding-bottom jump-to.constants/floating-shell-button-height}
        :render-fn                      activity-item
        :render-section-header-fn       (fn [{:keys [title]}] [quo/divider-date title])}])))
