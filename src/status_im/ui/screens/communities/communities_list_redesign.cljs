(ns status-im.ui.screens.communities.communities-list-redesign
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [quo2.components.separator :as separator]
            [quo2.components.markdown.text :as quo2.text]
            [quo2.components.buttons.button :as quo2.button]
            [quo2.components.counter.counter :as quo2.counter]
            [quo2.components.tags.tags :as tags]
            [quo2.foundations.colors :as quo2.colors]
            [quo.components.safe-area :as safe-area]
            [quo2.components.tabs.tabs :as quo2.tabs]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.react-native.resources :as resources]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.plus-button :as plus-button]
            [status-im.utils.handlers :refer [<sub  >evt]]
            [status-im.ui.components.topnav :as topnav]
            [quo2.components.community.community-card-view :as community-card]
            [quo2.components.community.community-list-view :as community-list]
            [quo2.components.icon :as icons]))

(def selected-tab (reagent/atom :all))
(def view-type   (reagent/atom  :card-view))
(def sort-list-by (reagent/atom :name))

(def mock-community-item-data ;; TODO: remove once communities are loaded with this data.
  {:data {:status :gated
          :locked true
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

(defn plus-button []
  (let [logging-in? (<sub [:multiaccounts/login])]
    [plus-button/plus-button
     {:on-press (when-not logging-in?
                  #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading logging-in?
      :accessibility-label :new-community-button}]))

(defn render-other-fn [community-item]
  (let [item (merge community-item
                    (get mock-community-item-data :data)
                    {:featured       false})]
    (if (= @view-type :card-view)
      [community-card/community-card-view-item item #(>evt [:navigate-to :community-overview item])]
      [community-list/communities-list-view-item item])))

(defn render-featured-fn [community-item]
  (let [item (merge community-item
                    (get mock-community-item-data :data)
                    {:featured       true})]
    [community-card/community-card-view-item item #(>evt [:navigate-to :community-overview item])]))

(defn get-item-layout-js [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(defn community-segments []
  [react/view {:flex               1
               :margin-bottom      8
               :padding-horizontal 20}
   [react/view {:flex-direction :row
                :padding-top    20
                :padding-bottom 8
                :height         60}
    [react/view {:flex 1}
     [quo2.tabs/tabs {:size           32
                      :on-change      #(reset! selected-tab %)
                      :default-active :all
                      :data           [{:id    :all
                                        :label (i18n/label :t/all)}
                                       {:id    :open
                                        :label (i18n/label :t/open)}
                                       {:id    :gated
                                        :label (i18n/label :t/gated)}]}]]
    [react/view {:flex-direction :row}
     [quo2.button/button
      {:icon     true
       :type     :outline
       :size     32
       :style    {:margin-right 12}
       :on-press #(re-frame/dispatch [:bottom-sheet/show-sheet :sort-communities {}])}
      :main-icons2/lightning]
     [quo2.button/button
      {:icon     true
       :type     :outline
       :size     32
       :on-press #(if (= @view-type :card-view)
                    (reset! view-type :list-view)
                    (reset! view-type :card-view))}
      (if (= @view-type :card-view)
        :main-icons2/card-view
        :main-icons2/list-view)]]]])

(defn featured-communities [communities]
  [list/flat-list
   {:key-fn                          :id
    :horizontal                        true
    :getItemLayout                     get-item-layout-js
    :keyboard-should-persist-taps      :always
    :shows-horizontal-scroll-indicator false
    :data                              communities
    :render-fn                         render-featured-fn}])

(defn other-communities [communities sort-list-by]
  (let [sorted-communities (sort-by sort-list-by communities)]
    [list/flat-list
     {:key-fn                            :id
      :getItemLayout                     get-item-layout-js
      :keyboard-should-persist-taps      :always
      :shows-horizontal-scroll-indicator false
      :data                              sorted-communities
      :render-fn                         render-other-fn}]))

(defn community-segments-view [communities]
  (let [tab @selected-tab
        sort-list-by @sort-list-by]
    (case tab
      :all
      [other-communities communities sort-list-by]

      :open
      [other-communities communities sort-list-by]

      :gated
      [other-communities communities sort-list-by])))

(defn featured-communities-section [communities]
  (let [count (reagent/atom {:value (count communities) :type :grey})]
    [react/view {:flex         1}
     [react/view {:flex-direction  :row
                  :height          30
                  :padding-top     8
                  :justify-content :space-between
                  :padding-horizontal 20}
      [react/view {:flex-direction  :row
                   :align-items     :center}
       [quo2.text/text {:accessibility-label :featured-communities-title
                        :weight              :semi-bold
                        :size                :paragraph-1
                        :style               {:margin-right   6}}
        (i18n/label :t/featured)]
       [quo2.counter/counter @count (:value @count)]]
      [icons/icon :main-icons2/info {:container-style {:align-items     :center
                                                       :justify-content :center}
                                     :resize-mode      :center
                                     :size             20
                                     :color            (quo2.colors/theme-colors
                                                        quo2.colors/neutral-50
                                                        quo2.colors/neutral-40)}]]
     [react/view {:margin-top     8
                  :padding-left   20}
      [featured-communities communities]]]))

(defn title-column []
  [react/view
   {:flex-direction     :row
    :align-items        :center
    :height             56
    :padding-vertical   12
    :padding-horizontal 20}
   [react/view
    {:flex           1}
    [quo2.text/text {:accessibility-label :communities-screen-title
                     :margin-right        6
                     :weight              :semi-bold
                     :size                :heading-1}
     (i18n/label :t/communities)]]
   [plus-button]])

(defn community-filter-tags []
  (let [filters [{:id 1 :tag-label (i18n/label :t/music) :resource (resources/get-image :music)}
                 {:id 2 :tag-label (i18n/label :t/lifestyle) :resource (resources/get-image :lifestyle)}
                 {:id 3 :tag-label (i18n/label :t/podcasts) :resource (resources/get-image :podcasts)}
                 {:id 4 :tag-label (i18n/label :t/podcasts) :resource (resources/get-image :podcasts)}]]
    [react/scroll-view {:horizontal                        true
                        :height                            48
                        :shows-horizontal-scroll-indicator false
                        :scroll-event-throttle             64
                        :padding-top                       4
                        :padding-bottom                    12
                        :padding-horizontal                20}
     [tags/tags {:data          filters
                 :labelled      true
                 :type          :emoji
                 :icon-color     (quo2.colors/theme-colors
                                  quo2.colors/neutral-50
                                  quo2.colors/neutral-40)}]]))

(defn communities-list []
  (let [multiaccount (<sub [:multiaccount])
        communities (<sub [:communities/communities])
        featured-communities (<sub [:communities/featured-communities])]

    [safe-area/consumer
     (fn [insets]
       [react/view {:style {:flex             1
                            :padding-top      (:top insets)
                            :background-color (quo2.colors/theme-colors
                                               quo2.colors/neutral-5
                                               quo2.colors/neutral-95)}}
        [topbar/topbar
         {:navigation      :none
          :left-component  [react/view {:margin-left 16}
                            [photos/photo (multiaccounts/displayed-photo multiaccount)
                             {:size 32}]]
          :right-component [react/view {:flex-direction :row
                                        :margin-right 16}
                            [topnav/qr-scanner]
                            [topnav/qr-code]
                            [topnav/notifications-button]]
          :new-ui?         true
          :border-bottom   false}]
        [title-column]
        [react/scroll-view
         [community-filter-tags]
         [featured-communities-section featured-communities]
         (when communities
           [:<>
            [react/view {:margin-vertical    4
                         :padding-horizontal 20}
             [separator/separator]]
            [community-segments]])
         [community-segments-view communities]]])]))
