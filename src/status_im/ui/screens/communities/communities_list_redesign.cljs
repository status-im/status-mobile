(ns status-im.ui.screens.communities.communities-list-redesign
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [quo2.components.community-card-view :as community-card-view]
            [quo2.components.separator :as separator]
            [quo2.components.text :as quo2.text]
            [quo2.components.button :as quo2.button]
            [quo2.components.counter :as quo2.counter]
            [quo2.components.filter-tags :as filter-tags]
            [quo2.components.filter-tag  :as filter-tag]
            [quo2.foundations.colors :as quo2.colors]
            [quo.components.safe-area :as safe-area]
            [status-im.qr-scanner.core :as qr-scanner]
            [quo2.components.tabs :as quo2.tabs]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.react-native.resources :as resources]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.plus-button :as plus-button]
            [status-im.utils.handlers :refer [<sub]]
            [quo2.components.icon :as icons])
  (:require-macros [status-im.utils.views :as views]))

(def selected-tab (reagent/atom :all))
(def view-type   (reagent/atom  :card-view))
(def sort-list-by (reagent/atom :name))

(defn qr-scanner []
  [quo2.button/button
   {:icon                true
    :size                32
    :type                :grey
    :style               {:margin-left 10}
    :accessibility-label :scan-qr-code-button
    :on-press #(re-frame/dispatch [::qr-scanner/scan-code
                                   {:title   (i18n/label :t/add-bootnode)
                                    :handler :bootnodes.callback/qr-code-scanned}])}
   :main-icons2/scanner])

(defn qr-code []
  [quo2.button/button
   {:icon                true
    :type                :grey
    :size                32
    :style               {:margin-left 10}
    :accessibility-label :contact-qr-code-button}
   :main-icons2/qr-code])

(views/defview notifications-button []
  (views/letsubs [notif-count [:activity.center/notifications-count]]
    [react/view
     [quo2.button/button {:icon                true
                          :type                :grey
                          :size                32
                          :style               {:margin-left 10}
                          :accessibility-label "notifications-button"
                          :on-press #(do
                                       (re-frame/dispatch [:mark-all-activity-center-notifications-as-read])
                                       (re-frame/dispatch [:navigate-to :notifications-center]))}
      :main-icons2/notifications]
     (when (pos? notif-count)
       [react/view {:style (merge (styles/counter-public-container) {:top 5 :right 5})
                    :pointer-events :none}
        [react/view {:style               styles/counter-public
                     :accessibility-label :notifications-unread-badge}]])]))

(views/defview plus-button []
  (views/letsubs [logging-in? [:multiaccounts/login]]
    [plus-button/plus-button
     {:on-press (when-not logging-in?
                  #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading logging-in?
      :accessibility-label :new-community-button}]))

(defn render-popular-fn [community-item]
  (if (= @view-type :card-view)
    [community-card-view/community-card-view community-item]
    [community-card-view/communities-list-view community-item]))

(defn render-featured-fn [community-item]
  [community-card-view/community-card-view community-item])

(defn community-list-key-fn [item]
  (:id item))

(defn get-item-layout [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(defn community-segments []
  [react/view {:flex               1
               :margin-bottom      8
               :padding-horizontal 20}
   [react/view {:flex-direction     :row
                :padding-top        20
                :padding-bottom     8
                :height             60}
    [react/view {:flex   1}
     [quo2.tabs/tabs {:size              32
                      :on-change         #(reset! selected-tab %)
                      :default-active    :all
                      :data [{:id :all   :label "All"}
                             {:id :open  :label "Open"}
                             {:id :gated :label "Gated"}]}]]
    [react/view {:flex-direction :row}
     [quo2.button/button
      {:icon                true
       :type                :outline
       :size                32
       :style               {:margin-right 12}
       :on-press            #(re-frame/dispatch [:bottom-sheet/show-sheet :sort-communities {}])}
      :main-icons2/lightning]
     [quo2.button/button
      {:icon                true
       :type                :outline
       :size                32
       :on-press            #(if (= @view-type :card-view)
                               (reset! view-type :list-view)
                               (reset! view-type :card-view))}
      (if (= @view-type :card-view)
        :main-icons2/card-view
        :main-icons2/list-view)]]]])

(views/defview featured-communities [communities]
  [list/flat-list
   {:key-fn                            community-list-key-fn
    :horizontal                        true
    :getItemLayout                     get-item-layout
    :keyboard-should-persist-taps      :always
    :shows-horizontal-scroll-indicator false
    :data                              communities
    :render-fn                         render-featured-fn}])

(views/defview popular-communities [communities sort-list-by]
  (let [sorted-communities (sort-by sort-list-by communities)]
    [list/flat-list
     {:key-fn                            community-list-key-fn
      :getItemLayout                     get-item-layout
      :keyboard-should-persist-taps      :always
      :shows-horizontal-scroll-indicator false
      :data                              sorted-communities
      :render-fn                         render-popular-fn}]))

(defn community-segments-view [communities]
  (let [tab @selected-tab
        sort-list-by @sort-list-by]
    [react/view {:padding-left 20}
     (cond
       (= tab :all)
       [popular-communities communities sort-list-by]

       (= tab :open)
       [popular-communities communities sort-list-by]

       (= tab :gated)
       [popular-communities communities sort-list-by])]))

(defn featured-communities-section [communities]
  (let [count (reagent/atom {:value 2 :type :grey})]
    [react/view {:padding-left 20}
     [react/view {:flex-direction  :row
                  :height          30
                  :padding-top     8
                  :justify-content :space-between
                  :margin-bottom   8}
      [react/view {:flex-direction  :row
                   :align-items     :center}
       [quo2.text/text {:accessibility-label :featured-communities-title
                        :style {:margin-right        6
                                :weight              :semi-bold
                                :size                :paragraph-1}}
        "Featured"]
       [quo2.counter/counter @count (:value @count)]]
      [react/view {:align-items  :center
                   :margin-right 20}
       [icons/icon :main-icons2/info {:container-style {:align-items     :center
                                                        :justify-content :center}
                                      :resize-mode      :center
                                      :size             20
                                      :color            (quo2.colors/theme-colors
                                                         quo2.colors/neutral-50
                                                         quo2.colors/neutral-40)}]]]
     [featured-communities communities]]))

(defn title-column []
  [react/view
   {:flex-direction     :row
    :align-items        :center
    :height             56
    :padding-vertical   12
    :padding-horizontal 16}
   [react/view
    {:flex           1}
    [quo2.text/text {:accessibility-label :communities-screen-title
                     :margin-right        6
                     :weight              :semi-bold
                     :size                :heading-1}
     "Communities"]]
   [plus-button]])

(views/defview community-filter-tags []
  (let [filters [{:id 1 :tag-label "Music" :resource (resources/get-image :music)}
                 {:id 2 :tag-label "Lifestyle" :resource (resources/get-image :lifestyle)}
                 {:id 3 :tag-label "Podcasts" :resource (resources/get-image :podcasts)}
                 {:id 3 :tag-label "NFT" :resource (resources/get-image :podcasts)}]
        icon-color (quo2.colors/theme-colors quo2.colors/black quo2.colors/white)]
    [react/scroll-view {:horizontal                        true
                        :height                            48
                        :shows-horizontal-scroll-indicator false
                        :scroll-event-throttle             64 
                        :padding-top                       4
                        :padding-bottom                    12
                        :padding-horizontal                20}
     [react/view {:flex-direction :row}
      [react/view {:margin-right  12}
       [filter-tag/filter-tag {:resource       :main-icons2/search
                               :labelled     false
                               :type           :icon
                               :icon-color     icon-color}]]
      [filter-tags/tags {:data          filters
                         :labelled      true
                         :type          :emoji}]]]))

(defn views []
  (let [multiaccount @(re-frame/subscribe [:multiaccount])
        communities (<sub [:communities/communities])]
    (fn []
      [safe-area/consumer
       (fn [insets]
         [react/view {:style {:flex             1
                              :padding-top      (:top insets)
                              :background-color (quo2.colors/theme-colors
                                                 quo2.colors/neutral-5
                                                 quo2.colors/neutral-95)}}
          [topbar/topbar
           {:navigation      :none
            :left-component  [react/view {:margin-left 12}
                              [photos/photo (multiaccounts/displayed-photo multiaccount)
                               {:size 32}]]
            :right-component [react/view {:flex-direction :row
                                          :margin-right 12}
                              [qr-scanner]
                              [qr-code]
                              [notifications-button]]
            :new-ui?         true
            :border-bottom   false}]
          [title-column]
          [react/scroll-view
           [community-filter-tags]
           [featured-communities-section communities]
           (when communities
             [react/view
              [react/view {:margin-vertical    4
                           :padding-horizontal 20}
               [separator/separator]]
              [community-segments]])
           [community-segments-view communities]]])])))