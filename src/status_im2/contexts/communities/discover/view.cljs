(ns status-im2.contexts.communities.discover.view
  (:require [i18n.i18n :as i18n]
            [oops.core :as oops] ;; TODO move to status-im2
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im.react-native.resources :as resources]
            [status-im2.contexts.communities.context-drawers.community-options.view :as options]
            [utils.re-frame :as rf]))

(def mock-community-item-data  ;; TODO: remove once communities are loaded with this data.
  {:data {:community-color "#0052FF"
          :status          :gated
          :locked?         true
          :cover           (resources/get-image :community-cover)
          :tokens          [{:id    1
                             :group [{:id         1
                                      :token-icon (resources/get-image :status-logo)}]}]
          :tags            [{:id        1
                             :tag-label (i18n/label :t/music)
                             :resource  (resources/get-image :music)}
                            {:id        2
                             :tag-label (i18n/label :t/lifestyle)
                             :resource  (resources/get-image :lifestyle)}
                            {:id        3
                             :tag-label (i18n/label :t/podcasts)
                             :resource  (resources/get-image :podcasts)}]}})

(defn render-fn
  [community-item _ _ {:keys [featured? width view-type]}]
  (let [item (merge community-item
                    (get mock-community-item-data :data)
                    {:featured featured?})]
    (if (= view-type :card-view)
      [quo/community-card-view-item (assoc item :width width)
       #(rf/dispatch [:navigate-to :community-overview (:id item)])]
      [quo/communities-list-view-item
       {:on-press      (fn []
                         (rf/dispatch [:communities/load-category-states (:id item)])
                         (rf/dispatch [:dismiss-keyboard])
                         (rf/dispatch [:navigate-to :community {:community-id (:id item)}]))
        :on-long-press #(rf/dispatch  [:bottom-sheet/show-sheet
                                       {:content (fn []
                                                   [options/community-options-bottom-sheet (:id item)])}])}])))

(defn screen-title
  []
  [rn/view
   {:height           56
    :padding-vertical 12}
   [quo/text
    {:accessibility-label :communities-screen-title
     :weight              :semi-bold
     :size                :heading-1}
    (i18n/label :t/discover-communities)]])

(defn featured-communities-header
  [communities-count]
  [rn/view
   {:flex-direction  :row
    :height          30
    :margin-bottom   8
    :justify-content :space-between}
   [rn/view
    {:flex-direction :row
     :align-items    :center}
    [quo/text
     {:accessibility-label :featured-communities-title
      :weight              :semi-bold
      :size                :paragraph-1
      :style               {:margin-right 6}}
     (i18n/label :t/featured)]
    [quo/counter {:type :grey} communities-count]]
   [quo/icon :i/info
    {:container-style {:align-items     :center
                       :justify-content :center}
     :resize-mode     :center
     :size            20
     :color           (colors/theme-colors
                       colors/neutral-50
                       colors/neutral-40)}]])

(defn featured-list
  [communities view-type]
  (let [view-size (reagent/atom 0)]
    (fn []
      [rn/view
       {:style     {:flex-direction :row
                    :overflow       :hidden
                    :width          "100%"
                    :margin-bottom  24}
        :on-layout #(swap! view-size
                           (fn []
                             (oops/oget % "nativeEvent.layout.width")))}
       (when-not (= @view-size 0)
         [rn/flat-list
          {:key-fn                            :id
           :horizontal                        true
           :keyboard-should-persist-taps      :always
           :shows-horizontal-scroll-indicator false
           :separator                         [rn/view {:width 12}]
           :data                              communities
           :render-fn                         render-fn
           :render-data                       {:featured? true
                                               :width     @view-size
                                               :view-type view-type}}])])))

(defn other-communities-list
  [communities view-type]
  [rn/flat-list
   {:key-fn                            :id
    :keyboard-should-persist-taps      :always
    :shows-horizontal-scroll-indicator false
    :separator                         [rn/view {:margin-bottom 16}]
    :data                              communities
    :render-fn                         render-fn
    :render-data                       {:featured? false
                                        :width     "100%"
                                        :view-type view-type}}])

(defn discover
  []
  (let [view-type (reagent/atom :card-view)]
    (fn []
      (let [communities                (rf/sub [:communities/sorted-communities])
            featured-communities       (rf/sub [:communities/featured-communities])
            featured-communities-count (count featured-communities)]
        [safe-area/consumer
         (fn []
           [rn/view
            {:style {:margin-left      20
                     :margin-right     20
                     :flex             1
                     :background-color (colors/theme-colors
                                        colors/white
                                        colors/neutral-90)}}
            [quo/button
             {:icon     true
              :type     :grey
              :size     32
              :style    {:margin-vertical 12}
              :on-press #(rf/dispatch [:navigate-back])}
             :i/close]
            [screen-title]
            [featured-communities-header featured-communities-count]
            [featured-list featured-communities @view-type]
            [other-communities-list communities @view-type]])]))))
