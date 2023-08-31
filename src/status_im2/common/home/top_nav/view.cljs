(ns status-im2.common.home.top-nav.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im.multiaccounts.core :as multiaccounts]
    [status-im2.common.home.top-nav.style :as style]
    [status-im2.constants :as constants]
    [utils.debounce :refer [dispatch-and-chill]]
    [utils.re-frame :as rf]))

(defn connectivity-sheet
  []
  (let [peers-count  (rf/sub [:peers-count])
        network-type (rf/sub [:network/type])]
    [rn/view
     [quo/text {:accessibility-label :peers-network-type-text} (str "NETWORK TYPE: " network-type)]
     [quo/text {:accessibility-label :peers-count-text} (str "PEERS COUNT: " peers-count)]]))

(defn view
  "[top-nav props]
  props
  {:blur? true/false
   :jump-to? true/false
   :container-style passed to outer view of component}"
  [{:keys [container-style blur? jump-to?]}]
  (let [{:keys [public-key]} (rf/sub [:profile/profile])
        online?              (rf/sub [:visibility-status-updates/online? public-key])
        account              (rf/sub [:profile/multiaccount])
        customization-color  (rf/sub [:profile/customization-color])
        avatar               {:online?         online?
                              :full-name       (multiaccounts/displayed-name account)
                              :profile-picture (multiaccounts/displayed-photo account)}
        network-type         (rf/sub [:network/type])
        unread-count         (rf/sub [:activity-center/unread-count])
        indicator            (rf/sub [:activity-center/unread-indicator])
        notification-type    (case indicator
                               :unread-indicator/seen :mention-seen ; should be `seen` - TODO discuss
                                                                    ; with design team about
                                                                    ; notifications for activity centre
                               :unread-indicator/new  :mention ; should be :notification TODO
                                                               ; https://github.com/status-im/status-mobile/issues/17102
                               nil)]
    [quo/top-nav
     {:avatar-on-press              #(rf/dispatch [:navigate-to :my-profile])
      :scan-on-press                #(js/alert "to be implemented")
      :activity-center-on-press     #(rf/dispatch [:activity-center/open])
      :qr-code-on-press             #(dispatch-and-chill [:open-modal :share-shell] 1000)
      :container-style              (merge style/top-nav-container container-style)
      :blur?                        blur?
      :jump-to?                     jump-to?
      :customization-color          customization-color
      :avatar-props                 avatar
      :max-unread-notifications     constants/activity-center-max-unread-count
      :notification-count           unread-count
      :notification                 notification-type
      :for-qa-only-cellular-network {:accessibility-label :on-cellular-network
                                     :on-press            #(rf/dispatch [:show-bottom-sheet
                                                                         {:content connectivity-sheet}])}
      :for-qa-only-no-network       {:accessibility-label :no-network-connection
                                     :on-press            #(rf/dispatch [:show-bottom-sheet
                                                                         {:content connectivity-sheet}])}
      :for-qa-only-network-type     network-type}]))
