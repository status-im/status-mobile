(ns status-im.ui2.screens.communities.discover-communities
  (:require [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [quo.react-native :as rn]
            [quo2.components.markdown.text :as quo2.text]
            [quo2.components.counter.counter :as quo2.counter]
            [quo.components.safe-area :as safe-area]
            [status-im.react-native.resources :as resources]
            [status-im.utils.handlers :refer [<sub >evt]]
            [quo2.components.buttons.button :as quo2.button]
            [quo2.components.community.community-card-view :as community-card]
            [quo2.components.community.community-list-view :as community-list]
            [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]
            [status-im.ui.screens.communities.community :as community]))

(def view-type   (reagent/atom  :card-view))

(def mock-community-item-data ;; TODO: remove once communities are loaded with this data.
  {:data {:community-color "#0052FF"
          :status  :gated
          :locked? true
          :cover  (resources/get-image :community-cover)
          :tokens [{:id    1
                    :group [{:id         1
                             :token-icon (resources/get-image :status-logo)}]}]
          :tags   [{:id        1
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
                    {:featured       false})]
    (if (= @view-type :card-view)
      [community-card/community-card-view-item item #(>evt [:navigate-to :community-overview item])]
      [community-list/communities-list-view-item
       {:on-press      (fn []
                         (>evt [:communities/load-category-states (:id item)])
                         (>evt [:dismiss-keyboard])
                         (>evt [:navigate-to :community {:community-id (:id item)}]))
        :on-long-press #(>evt [:bottom-sheet/show-sheet
                               {:content (fn []
                                           [community/community-actions item])}])}
       item])))

(defn render-featured-fn [community-item]
  (let [item (merge community-item
                    (get mock-community-item-data :data)
                    {:featured       true})]
    [community-card/community-card-view-item item #(>evt [:navigate-to :community-overview item])]))

(defn get-item-layout-js [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(defn screen-title []
  [rn/view
   {:height             56
    :padding-vertical   12
    :padding-horizontal 20}
   [quo2.text/text {:accessibility-label :communities-screen-title
                    :weight              :semi-bold
                    :size                :heading-1}
    (i18n/label :t/discover-communities)]])

(defn featured-communities [communities]
  [list/flat-list
   {:key-fn                          :id
    :horizontal                        true
    :getItemLayout                     get-item-layout-js
    :keyboard-should-persist-taps      :always
    :shows-horizontal-scroll-indicator false
    :data                              communities
    :render-fn                         render-featured-fn}])

(defn featured-communities-section [communities]
  (let [count (reagent/atom {:value (count communities) :type :grey})]
    [rn/view {:flex         1}
     [rn/view {:flex-direction  :row
               :height          30
               :padding-top     8
               :justify-content :space-between
               :padding-horizontal 20}
      [rn/view {:flex-direction  :row
                :align-items     :center}
       [quo2.text/text {:accessibility-label :featured-communities-title
                        :weight              :semi-bold
                        :size                :paragraph-1
                        :style               {:margin-right   6}}
        (i18n/label :t/featured)]
       [quo2.counter/counter @count (:value @count)]]
      [icons/icon :i/info {:container-style {:align-items     :center
                                             :justify-content :center}
                           :resize-mode      :center
                           :size             20
                           :color            (colors/theme-colors
                                              colors/neutral-50
                                              colors/neutral-40)}]]
     [rn/view {:margin-top     8
               :padding-left   20}
      [featured-communities communities]]]))

(defn other-communities [communities sort-list-by]
  (let [sorted-communities (sort-by sort-list-by communities)]
    [list/flat-list
     {:key-fn                            :id
      :getItemLayout                     get-item-layout-js
      :keyboard-should-persist-taps      :always
      :shows-horizontal-scroll-indicator false
      :header                            (featured-communities-section communities)
      :data                              sorted-communities
      :render-fn                         render-other-fn}]))

(defn discover-communities []
  (let [communities (<sub [:communities/communities])]
    [rn/view {:flex             1}
     [quo2.button/button {:icon     true
                          :type     :grey
                          :size     32
                          :style    {:margin-vertical 12
                                     :margin-left     20}
                          :on-press #(>evt [:navigate-back])}
      :close]
     [screen-title]
     [other-communities communities :name]]))

(defn communities []
  (fn []
    [safe-area/consumer
     (fn []
       [rn/view {:style {:flex             1
                         :background-color (colors/theme-colors
                                            colors/white
                                            colors/neutral-90)}}
        [discover-communities]])]))
