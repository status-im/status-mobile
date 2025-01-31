(ns status-im.contexts.wallet.common.activity-tab.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.shell.constants :as shell.constants]
    [status-im.contexts.wallet.common.activity-tab.activity-types.view :as activity-type]
    [status-im.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- section-header
  [{:keys [title]}]
  [quo/divider-date title])

(defn- activity-item
  [{:keys [tx-type] :as activity}]
  (case tx-type
    :send     [activity-type/send-activity activity]
    :bridge   [activity-type/bridge-activity activity]
    :swap     [activity-type/swap-activity activity]
    :approval [activity-type/approval-activity activity]
    nil))

(defn view
  []
  (let [theme          (quo.theme/use-theme)
        loading?       (rf/sub [:wallet/activity-tab-loading?])
        activity-list  (rf/sub [:wallet/activities-for-current-viewing-account])
        on-end-reached (rn/use-callback
                        #(rf/dispatch
                          [:wallet/get-more-for-activities-filter-session]))]
    (if (and (and (some? loading?) (false? loading?)) (empty? activity-list))
      [empty-tab/view
       {:title       (i18n/label :t/no-activity)
        :description (i18n/label :t/empty-tab-description)
        :image       (resources/get-themed-image :no-activity theme)}]
      [rn/section-list
       {:sections                        activity-list
        :sticky-section-headers-enabled  false
        :style                           {:flex               1
                                          :padding-horizontal 8}
        :content-container-style         {:padding-bottom shell.constants/floating-shell-button-height}
        :shows-vertical-scroll-indicator false
        :render-fn                       activity-item
        :render-section-header-fn        section-header
        :on-end-reached                  on-end-reached
        :on-end-reached-threshold        2}])))
