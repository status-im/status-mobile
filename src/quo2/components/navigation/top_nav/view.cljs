(ns quo2.components.navigation.top-nav.view
  (:require [react-native.core :as rn]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.counter.counter.view :as counter]
            [quo2.components.navigation.top-nav.style :as style]
            [react-native.hole-view :as hole-view]
            [quo2.theme :as quo.theme]
            [quo2.components.common.notification-dot.view :as notification-dot]))

(def notification-dot-hole
  [{:x            37
    :y            -2
    :width        9
    :height       9
    :borderRadius 4}])

(defn unread-counter-hole
  [notification-count]
  (let [x     (case (count (str notification-count))
                (1 2) 33
                29)
        width (case (count (str notification-count))
                1 16
                2 20
                28)]
    (if (pos? (js/Number notification-count))
      [{:x            x
        :y            -5
        :width        width
        :height       16
        :borderRadius 6}]
      [])))

(defn- get-button-common-props
  [{:keys [jump-to? theme blur?]}]
  {:icon-only?      true
   :size            32
   :container-style {:margin-left 12}
   :type            (cond
                      jump-to?                          :black
                      (and (not blur?) (= :dark theme)) :dark-grey
                      :else                             :grey)
   :background      (when blur? :blur)})

(defn- unread-indicator
  [{:keys [notification-count max-value customization-color type]}]
  (when (pos? notification-count)
    [counter/view
     {:accessibility-label :activity-center-unread-count
      :type                type
      :customization-color customization-color
      :container-style     (style/unread-indicator notification-count
                                                   max-value)}
     notification-count]))

(defn unread-dot
  [{:keys [customization-color blur? notification]}]
  [notification-dot/view
   {:style               style/unread-dot
    :blur?               blur?
    :customization-color (when (= notification :notification) customization-color)}])

(defn notification-highlight
  [{:keys [notification notification-count
           max-unread-notifications customization-color]
    :as   props}]
  (case notification
    (:seen :notification)    [unread-dot props]
    (:mention :mention-seen) [unread-indicator
                              {:type                (if (= notification :mention-seen) :grey :default)
                               :notification-count  notification-count
                               :max-value           max-unread-notifications
                               :customization-color customization-color}]
    [:<>]))

(defn- left-section
  [{:keys [avatar-props on-press customization-color]}]
  [rn/touchable-without-feedback {:on-press on-press}
   [rn/view
    {:accessibility-label :open-profile}
    [user-avatar/user-avatar
     (merge {:status-indicator?   true
             :ring?               true
             :customization-color customization-color
             :size                :small}
            avatar-props)]]])

(defn- right-section
  [{:keys [theme
           jump-to?
           blur?
           notification
           notification-count
           activity-center-on-press
           scan-on-press
           qr-code-on-press
           for-qa-only-cellular-network
           for-qa-only-no-network
           for-qa-only-network-type]
    :as   props}]
  (let [button-common-props (get-button-common-props {:theme    theme
                                                      :jump-to? jump-to?
                                                      :blur?    blur?})]
    [rn/view {:style style/right-section}
     (when (= for-qa-only-network-type "cellular")
       [button/button
        (merge (dissoc button-common-props :icon-only?)
               for-qa-only-cellular-network)
        "🦄"])
     (when (= for-qa-only-network-type "none")
       [button/button
        (merge (dissoc button-common-props :icon-only?)
               for-qa-only-no-network)
        "💀"])
     [button/button
      (assoc button-common-props :accessibility-label :open-scanner-button :on-press scan-on-press)
      :i/scan]
     [button/button
      (merge button-common-props
             {:accessibility-label :show-qr-button
              :on-press            qr-code-on-press})
      :i/qr-code]
     [rn/view
      [hole-view/hole-view
       {:key   (hash (str notification notification-count))
        :holes (case notification
                 (:seen :notification)    notification-dot-hole
                 (:mention :mention-seen) (unread-counter-hole notification-count)
                 [])}
       [button/button
        (merge button-common-props
               {:accessibility-label :open-activity-center-button
                :on-press            activity-center-on-press})
        :i/activity-center]]
      [notification-highlight props]]]))

(defn view-internal
  [{:keys [avatar-on-press avatar-props customization-color container-style] :as props}]
  [rn/view {:style (merge style/top-nav-container container-style)}
   [rn/view {:style style/top-nav-inner-container}
    [left-section
     {:avatar-props        avatar-props
      :on-press            avatar-on-press
      :customization-color customization-color}]
    [right-section (dissoc props :avatar-props :avatar-on-press)]]])

(def view
  ":container-style style map merged with outer view for margins/paddings
   :customization-color custom colors
   :blur? true/false
   :jump-to? true/false
   :theme :light/:dark
   :notification :mention/:seen/:notification (TODO :mention-seen temporarily used while resolving https://github.com/status-im/status-mobile/issues/17102 )
   :avatar-props qu2/user-avatar props
   :avatar-on-press callback
   :scan-on-press callback
   :activity-center-on-press callback
   :qr-code-on-press callback
   :notification-count number
   :max-unread-notifications used to specify max number for counter
   :for-qa-only-cellular-network used for testing purposed
   :for-qa-only-no-network used for testing purposed
   :for-qa-only-network-type used for testing purposed
   "
  (quo.theme/with-theme view-internal))
