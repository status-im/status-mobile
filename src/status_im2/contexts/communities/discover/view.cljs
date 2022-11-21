(ns status-im2.contexts.communities.discover.view
  (:require [reagent.core :as reagent]
            [i18n.i18n :as i18n]
            [react-native.safe-area :as safe-area]
            [react-native.core :as rn]
            [quo2.core :as quo]
            [quo2.components.community.community-card-view :as community-card]
            [quo2.components.community.community-list-view :as community-list]
            [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]
            [utils.re-frame :as rf]

            ;; TODO move to status-im2
            [status-im.react-native.resources :as resources]
            [status-im.ui.screens.communities.community :as community]))

(def view-type (reagent/atom :card-view))

(def mock-community-item-data                               ;; TODO: remove once communities are loaded with this data.
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

(defn render-other-fn [community-item]
  (let [item (merge community-item
                    (get mock-community-item-data :data)
                    {:featured false})]
    (if (= @view-type :card-view)
      [community-card/community-card-view-item item #(rf/dispatch [:navigate-to :community-overview item])]
      [community-list/communities-list-view-item
       {:on-press      (fn []
                         (rf/dispatch [:communities/load-category-states (:id item)])
                         (rf/dispatch [:dismiss-keyboard])
                         (rf/dispatch [:navigate-to :community {:community-id (:id item)}]))
        :on-long-press #(rf/dispatch [:bottom-sheet/show-sheet
                                      {:content (fn []
                                                  ;; TODO implement with quo2
                                                  [community/community-actions item])}])}
       item])))

(defn render-featured-fn [community-item]
  (let [item (merge community-item
                    (get mock-community-item-data :data)
                    {:featured true})]
    [community-card/community-card-view-item item #(rf/dispatch [:navigate-to :community-overview item])]))

(defn get-item-layout-js [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(defn screen-title []
  [rn/view
   {:height             56
    :padding-vertical   12
    :padding-horizontal 20}
   [quo/text {:accessibility-label :communities-screen-title
              :weight              :semi-bold
              :size                :heading-1}
    (i18n/label :t/discover-communities)]])

(defn featured-communities [communities]
  [rn/flat-list
   {:key-fn                            :id
    :horizontal                        true
    :getItemLayout                     get-item-layout-js
    :keyboard-should-persist-taps      :always
    :shows-horizontal-scroll-indicator false
    :data                              communities
    :render-fn                         render-featured-fn}])

(defn featured-communities-section [communities]
  (let [count (reagent/atom {:value (count communities) :type :grey})]
    [rn/view {:flex 1}
     [rn/view {:flex-direction     :row
               :height             30
               :padding-top        8
               :justify-content    :space-between
               :padding-horizontal 20}
      [rn/view {:flex-direction :row
                :align-items    :center}
       [quo/text {:accessibility-label :featured-communities-title
                  :weight              :semi-bold
                  :size                :paragraph-1
                  :style               {:margin-right 6}}
        (i18n/label :t/featured)]
       [quo/counter @count (:value @count)]]
      [icons/icon :i/info {:container-style {:align-items     :center
                                             :justify-content :center}
                           :resize-mode     :center
                           :size            20
                           :color           (colors/theme-colors
                                             colors/neutral-50
                                             colors/neutral-40)}]]
     [rn/view {:margin-top   8
               :padding-left 20}
      [featured-communities communities]]]))

(defn other-communities []
  (let [communities (rf/sub [:communities/communities])
        ;;TODO move sorting to subscription
        sorted-communities (sort-by :name communities)]
    [rn/flat-list
     {:key-fn                            :id
      :getItemLayout                     get-item-layout-js
      :keyboard-should-persist-taps      :always
      :shows-horizontal-scroll-indicator false
      :header                            (featured-communities-section communities)
      :data                              sorted-communities
      :render-fn                         render-other-fn}]))

(defn discover []
  [safe-area/consumer
   (fn []
     [rn/view {:style {:flex             1
                       :background-color (colors/theme-colors
                                          colors/white
                                          colors/neutral-90)}}
      [quo/button {:icon     true
                   :type     :grey
                   :size     32
                   :style    {:margin-vertical 12
                              :margin-left     20}
                   :on-press #(rf/dispatch [:navigate-back])}
       :i/close]
      [screen-title]
      [other-communities]])])
