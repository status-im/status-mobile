(ns status-im2.common.home.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.common.home.style :as style]
            [status-im2.common.plus-button.view :as plus-button]
            [utils.re-frame :as rf]))

(defn title-column
  [{:keys [label handler accessibility-label]}]
  [rn/view style/title-column
   [rn/view {:flex 1}
    [quo/text style/title-column-text
     label]]
   [plus-button/plus-button
    {:on-press            handler
     :accessibility-label accessibility-label}]])

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
       {:type  unread-type
        :style (style/unread-indicator unread-count)}
       unread-count])))

(defn- left-section
  [{:keys [avatar]}]
  [rn/touchable-without-feedback {:on-press #(rf/dispatch [:navigate-to :my-profile])}
   [rn/view
    {:accessibility-label :open-profile
     :style               style/left-section}
    [quo/user-avatar
     (merge {:status-indicator? true
             :size              :small}
            avatar)]]])

(defn- right-section
  [{:keys [button-type search?]}]
  (let [button-common-props (get-button-common-props button-type)]
    [rn/view {:style style/right-section}
     (when search?
       [quo/button
        (assoc button-common-props :accessibility-label :open-search-button)
        :i/search])
     [quo/button
      (assoc button-common-props :accessibility-label :open-scanner-button)
      :i/scan]
     [quo/button
      (assoc button-common-props :accessibility-label :show-qr-button)
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
   :avatar  user-avatar
   :search? When non-nil, show search button}
  "
  [{:keys [type style avatar search?]
    :or   {type :default}}]
  [rn/view {:style (style/top-nav-container style)}
   [left-section {:avatar avatar}]
   [right-section {:button-type type :search? search?}]])
