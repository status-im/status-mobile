(ns status-im2.common.home.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.navigation :as navigation]
    [reagent.core :as reagent]
    [status-im2.common.home.style :as style]
    [status-im2.common.plus-button.view :as plus-button]
    [status-im2.constants :as constants]
    [status-im2.contexts.activity-center.view :as ac]
    [status-im2.contexts.shell.animation :as shell]
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
       {:accessibility-label :activity-center-unread-count
        :type                unread-type
        :style               (style/unread-indicator unread-count
                                                     constants/activity-center-max-unread-count)}
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

(defn connectivity-sheet
  []
  (let [peers-count  (rf/sub [:peers-count])
        network-type (rf/sub [:network/type])]
    [rn/view
     [quo/text {:accessibility-label :peers-network-type-text} (str "NETWORK TYPE: " network-type)]
     [quo/text {:accessibility-label :peers-count-text} (str "PEERS COUNT: " peers-count)]]))

(defn ac-modal
  [visible? view-id]
  (let [status-bar-style (if (or (colors/dark?)
                                 (not (shell/home-stack-open?)))
                           :light
                           :dark)
        close-modal      (fn []
                           (reset! visible? false)
                           (navigation/merge-options (clj->js view-id)
                                                     (clj->js {:statusBar {:style status-bar-style}})))]
    [rn/modal
     {:visible             @visible?
      :cover-screen           true
      :transparent            true
      :status-bar-translucent true
      :hardware-accelerated   true
      :animation-type         :slide
      :style                  {:margin 0
                               :width  "100%"}
      :on-back-button-press   close-modal}
     [ac/view close-modal]]))

(defn- right-section
  [{:keys [button-type search?]}]
  (let [button-common-props (get-button-common-props button-type)
        network-type        (rf/sub [:network/type])
        visible?               (reagent/atom false)
        view-id                (rf/sub [:view-id])
        open-ac                (fn []
                                 ;; delaying status-bar style update looks nicer
                                 (js/setTimeout
                                   #(navigation/merge-options (clj->js view-id)
                                                              (clj->js {:statusBar {:style :light}}))
                                   20)
                                 (reset! visible? (not @visible?)))]
    [rn/view {:style style/right-section}
     [ac-modal visible? view-id]
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
              :on-press            #(rf/dispatch [:open-modal :share-shell])})
      :i/qr-code]
     [rn/view
      [unread-indicator]
      [quo/button
       (merge button-common-props
              {:accessibility-label :open-activity-center-button
               ;:on-press            #(rf/dispatch [:activity-center/open])
               :on-press            open-ac
               })
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
  [rn/view {:style (merge style/top-nav-container style)}
   [left-section {:avatar avatar}]
   [right-section {:button-type type :search? search?}]])
