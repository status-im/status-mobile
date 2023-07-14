(ns status-im2.common.home.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im2.common.home.style :as style]
    [status-im.multiaccounts.core :as multiaccounts]
    [status-im2.common.plus-button.view :as plus-button]
    [status-im2.constants :as constants]
    [utils.re-frame :as rf]
    [utils.debounce :refer [dispatch-and-chill]]))

(defn title-column
  [{:keys [label handler accessibility-label customization-color]}]
  [rn/view style/title-column
   [rn/view {:flex 1}
    [quo/text style/title-column-text
     label]]
   [plus-button/plus-button
    {:on-press            handler
     :accessibility-label accessibility-label
     :customization-color customization-color}]])

(defn- get-button-common-props
  [type]
  (let [default? (= type :default)
        dark?    (colors/dark?)]
    {:icon                      true
     :size                      32
     :style                     {:margin-left 12}
     :type                      (if default?
                                  (if dark? :grey :dark-grey)
                                  type)
     :override-background-color (when (and dark? default?)
                                  colors/neutral-90)}))

(defn- unread-indicator
  []
  (let [unread-count (rf/sub [:activity-center/unread-count])
        indicator    (rf/sub [:activity-center/unread-indicator])
        unread-type  (case indicator
                       :unread-indicator/seen :grey
                       :unread-indicator/new  :default
                       nil)]
    (when (pos? unread-count)
      [quo/counter
       {:accessibility-label :activity-center-unread-count
        :type                unread-type
        :style               (style/unread-indicator unread-count
                                                     constants/activity-center-max-unread-count)}
       unread-count])))

(defn- left-section
  [{:keys [avatar]}]
  (let [{:keys [public-key]} (rf/sub [:profile/profile])
        online?              (rf/sub [:visibility-status-updates/online? public-key])]
    [rn/touchable-without-feedback {:on-press #(rf/dispatch [:navigate-to :my-profile])}
     [rn/view
      {:accessibility-label :open-profile
       :style               style/left-section}
      [quo/user-avatar
       (merge {:status-indicator? true
               :size              :small
               :online?           online?}
              avatar)]]]))

(defn connectivity-sheet
  []
  (let [peers-count  (rf/sub [:peers-count])
        network-type (rf/sub [:network/type])]
    [rn/view
     [quo/text {:accessibility-label :peers-network-type-text} (str "NETWORK TYPE: " network-type)]
     [quo/text {:accessibility-label :peers-count-text} (str "PEERS COUNT: " peers-count)]]))

(defn- right-section
  [{:keys [button-type search?]}]
  (let [button-common-props (get-button-common-props button-type)
        network-type        (rf/sub [:network/type])]
    [rn/view {:style style/right-section}
     (when (= network-type "cellular")
       [quo/button
        (merge button-common-props
               {:icon                false
                :accessibility-label :on-cellular-network
                :on-press            #(rf/dispatch [:show-bottom-sheet
                                                    {:content connectivity-sheet}])})
        "🦄"])
     (when (= network-type "none")
       [quo/button
        (merge button-common-props
               {:icon                false
                :accessibility-label :no-network-connection
                :on-press            #(rf/dispatch [:show-bottom-sheet
                                                    {:content connectivity-sheet}])})
        "💀"])
     (when search?
       [quo/button
        (assoc button-common-props :accessibility-label :open-search-button)
        :i/search])
     [quo/button
      (assoc button-common-props :accessibility-label :open-scanner-button)
      :i/scan]
     [quo/button
      (merge button-common-props
             {:accessibility-label :show-qr-button
              :on-press            #(dispatch-and-chill [:open-modal :share-shell] 1000)})
      :i/qr-code]
     [rn/view
      [unread-indicator]
      [quo/button
       (merge button-common-props
              {:accessibility-label :open-activity-center-button
               :on-press            #(rf/dispatch [:activity-center/open])})
       :i/activity-center]]]))

(defn top-nav
  "[top-nav props]
  props
  {:type    quo/button types
   :style   override-style
   :search? When non-nil, show search button}
  "
  [{:keys [type style search?]
    :or   {type :default}}]
  (let [account             (rf/sub [:profile/multiaccount])
        customization-color (rf/sub [:profile/customization-color])
        avatar              {:customization-color customization-color
                             :full-name           (multiaccounts/displayed-name account)
                             :profile-picture     (multiaccounts/displayed-photo account)}]
    [rn/view {:style (merge style/top-nav-container style)}
     [left-section {:avatar avatar}]
     [right-section {:button-type type :search? search?}]]))
