(ns status-im.ui.screens.communities.communities-list-redesign
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.screens.communities.community-views-redesign :as community-views]
            [quo2.components.separator :as separator]
            [quo2.components.text :as quo2.text]
            [quo2.components.button :as quo2.button]
            [quo2.components.counter :as quo2.counter]
            [quo2.components.filter-tags :as filter-tags]
            [quo2.components.filter-tag  :as filter-tag]
            [quo2.foundations.colors :as quo2.colors]
            [quo.design-system.colors :as colors]
            [quo.components.safe-area :as safe-area]
            [status-im.qr-scanner.core :as qr-scanner]
            [quo2.components.tabs :as quo2.tabs]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.constants :as constants]
            [status-im.react-native.resources :as resources]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.topbar-redesign :as topbar]
            [status-im.ui.components.plus-button-redesign :as plus-button]
            [quo2.components.icon :as icons])
  (:require-macros [status-im.utils.views :as views]))

(def selected-tab (reagent/atom :all))
(def view-style   (reagent/atom :card-view))
(def sort-list-by (reagent/atom :name))

(def icon-color (quo2.colors/theme-colors
                 quo2.colors/neutral-50
                 quo2.colors/neutral-40))

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
  (if (= @view-style :card-view)
    [community-views/community-card-view-item community-item]
    [community-views/communities-list-view-item community-item]))

(defn render-featured-fn [community-item]
  [community-views/community-card-view-item community-item])

(defn community-list-key-fn [item]
  (:id item))

(defn get-item-layout [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(def communities-items-featured
  {:data [{:id             constants/status-community-id
           :name           "Status"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :status         "gated"
           :section        "featured"
           :permissions-granted true
           :cover          (resources/get-image :community-cover-image)
           :community-icon (resources/get-image :status-logo)
           :color          (rand-nth colors/chat-colors)
           :token-groups [{:id  1 :tokens [{:id 1 :token-icon (resources/get-image :status-logo)}]}]
           :tags [{:id 1 :label "Crypto" :emoji (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :emoji (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :emoji (resources/reactions :thumbs-up)}]}
          {:id             constants/status-community-id
           :name           "Status"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :status         "gated"
           :section        "featured"
           :cover          (resources/get-image :community-cover-image)
           :community-icon (resources/get-image :status-logo)
           :color          (rand-nth colors/chat-colors)
           :token-groups [{:id  1 :tokens [{:id 1 :token-icon (resources/get-image :status-logo)}]}]
           :tags [{:id 1 :label "Crypto" :emoji (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :emoji (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :emoji (resources/reactions :thumbs-up)}]}
          {:id             constants/status-community-id
           :name           "Status"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :status         "gated"
           :section        "featured"
           :cover          (resources/get-image :community-cover-image)
           :community-icon (resources/get-image :status-logo)
           :token-groups [{:id  1 :tokens [{:id 1 :token-icon (resources/get-image :status-logo)}]}]
           :color          (rand-nth colors/chat-colors)
           :tags [{:id 1 :label "Crypto" :emoji (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :emoji (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :emoji (resources/reactions :thumbs-up)}]}]})

(def communities-items-popular
  {:data [{:id             constants/status-community-id
           :name           "Status"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :status         "gated"
           :section        "popular"
           :permissions    true
           :cover          (resources/get-image :community-cover-image)
           :community-icon (resources/get-image :status-logo)
           :color          (rand-nth colors/chat-colors)
           :token-groups   [{:id  1 :tokens [{:id 1 :token-icon (resources/get-image :status-logo)}
                                             {:id 2 :token-icon (resources/get-image :status-logo)}]}
                            {:id  2 :tokens [{:id 1 :token-icon (resources/get-image :status-logo)}
                                             {:id 2 :token-icon (resources/get-image :status-logo)}
                                             {:id 3 :token-icon (resources/get-image :status-logo)}
                                             {:id 4 :token-icon (resources/get-image :status-logo)}]}]
           :tags [{:id 1 :label "Crypto" :emoji (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :emoji (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :emoji (resources/reactions :thumbs-up)}]}
          {:id             2
           :name           "Politics"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :status         "gated"
           :section        "popular"
           :permissions    true
           :community-icon (resources/get-image :status-logo)
           :color          (rand-nth colors/chat-colors)
           :token-groups   [{:tokens [{:id 1 :token-icon (resources/get-image :status-logo)}
                                      {:id 2 :token-icon (resources/get-image :status-logo)}
                                      {:id 2 :token-icon (resources/get-image :status-logo)}]}
                            {:id  2 :tokens [{:id 1 :token-icon (resources/get-image :status-logo)}
                                             {:id 2 :token-icon (resources/get-image :status-logo)}]}]
           :tags [{:id 1 :label "Crypto" :emoji (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :emoji (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :emoji (resources/reactions :thumbs-up)}]}
          {:id             3
           :name           "Sports"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :status         "gated"
           :section        "popular"
           :permissions    false
           :community-icon (resources/get-image :status-logo)
           :color          (rand-nth colors/chat-colors)
           :token-groups   [{:id  1 :tokens [{:id 1 :token-icon (resources/get-image :status-logo)}
                                             {:id 2 :token-icon (resources/get-image :status-logo)}
                                             {:id 3 :token-icon (resources/get-image :status-logo)}
                                             {:id 4 :token-icon (resources/get-image :status-logo)}]}
                            {:id  2 :tokens [{:id 1 :token-icon (resources/get-image :status-logo)}
                                             {:id 2 :token-icon (resources/get-image :status-logo)}
                                             {:id 3 :token-icon (resources/get-image :status-logo)}
                                             {:id 4 :token-icon (resources/get-image :status-logo)}]}]
           :tags [{:id 1 :label "Crypto" :emoji (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :emoji (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :emoji (resources/reactions :thumbs-up)}]}
          {:id             4
           :name           "News"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :status         "gated"
           :section        "popular"
           :permissions    true
           :community-icon (resources/get-image :status-logo)
           :color          (rand-nth colors/chat-colors)
           :token-groups   [{:id  1 :tokens [{:id 1 :token-icon (resources/get-image :status-logo)}
                                             {:id 2 :token-icon (resources/get-image :status-logo)}
                                             {:id 3 :token-icon (resources/get-image :status-logo)}
                                             {:id 4 :token-icon (resources/get-image :status-logo)}
                                             {:id 5 :token-icon (resources/get-image :status-logo)}]}]
           :tags [{:id 1 :label "Crypto" :emoji (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :emoji (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :emoji (resources/reactions :thumbs-up)}]}
          {:id             5
           :name           "Technology"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :status         "gated"
           :section        "popular"
           :permissions    false
           :community-icon (resources/get-image :status-logo)
           :color          (rand-nth colors/chat-colors)
           :token-groups [{:id  1 :tokens [{:id 1 :token-icon (resources/get-image :status-logo)}
                                           {:id 2 :token-icon (resources/get-image :status-logo)}]}
                          {:id  2 :tokens [{:id 1 :token-icon (resources/get-image :status-logo)}
                                           {:id 2 :token-icon (resources/get-image :status-logo)}
                                           {:id 3 :token-icon (resources/get-image :status-logo)}
                                           {:id 4 :token-icon (resources/get-image :status-logo)}]}
                          {:id  3 :tokens [{:id 1 :token-icon (resources/get-image :status-logo)}
                                           {:id 2 :token-icon (resources/get-image :status-logo)}
                                           {:id 3 :token-icon (resources/get-image :status-logo)}
                                           {:id 4 :token-icon (resources/get-image :status-logo)}
                                           {:id 5 :token-icon (resources/get-image :status-logo)}]}]
           :tags [{:id 1 :label "Crypto" :emoji (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :emoji (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :emoji (resources/reactions :thumbs-up)}]}]})

(defn community-tabs []
  [react/view {:flex               1
               :padding-horizontal 20}
   [react/view {:flex-direction     :row
                :align-items        :center
                :padding-top        20
                :padding-bottom     8}
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
       :on-press            #(re-frame/dispatch [:bottom-sheet-redesign/show-sheet :sort-communities {}])}
      :main-icons2/lightning]
     [quo2.button/button
      {:icon                true
       :type                :outline
       :size                32
       :on-press            #(if (= @view-style :card-view)
                               (reset! view-style :list-view)
                               (reset! view-style :card-view))}
      (if (= @view-style :card-view)
        :main-icons2/card-view
        :main-icons2/list-view)]]]])

(views/defview featured-communities []
  (let [items (get communities-items-featured :data)]
    [list/flat-list
     {:key-fn                            community-list-key-fn
      :horizontal                        true
      :getItemLayout                     get-item-layout
      :keyboard-should-persist-taps      :always
      :shows-horizontal-scroll-indicator false
      :data                              items
      :render-fn                         render-featured-fn}]))

(views/defview popular-communities [sort-list-by]
  (let [items (get communities-items-popular :data)
        sorted-items (sort-by sort-list-by items)]
    [list/flat-list
     {:key-fn                            community-list-key-fn
      :getItemLayout                     get-item-layout
      :keyboard-should-persist-taps      :always
      :shows-horizontal-scroll-indicator false
      :data                              sorted-items
      :render-fn                         render-popular-fn}]))

(defn community-tabs-view []
  (let [tab @selected-tab
        sort-list-by @sort-list-by]
    [react/view {:padding-horizontal  20}
     (cond
       (= tab :all)
       [react/view
        [popular-communities sort-list-by]]
       (= tab :open)
       [react/view
        [popular-communities sort-list-by]]
       (= tab :gated)
       [react/view
        [popular-communities sort-list-by]])]))

(defn featured-communities-section []
  (let [count (reagent/atom {:value 2 :type :grey})]
    [:<>
     [react/view {:padding-left 20}
      [react/view {:flex-direction  :row
                   :align-item      :center
                   :justify-content :space-between
                   :padding-bottom  8}
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
                                       :color            icon-color}]]]
      [featured-communities]]]))

(defn title-column []
  [react/view
   {:flex-direction     :row
    :align-items        :center
    :padding-vertical   12
    :padding-horizontal 20}
   [react/view
    {:flex           1}
    [quo2.text/text {:accessibility-label :communities-screen-title
                     :margin-right        6
                     :weight              :semi-bold
                     :size                :heading-1}
     "Communities"]]
   [plus-button]])

(views/defview community-filter-tags []
  (let [filters [{:id 1 :label "Crypto"  :emoji (resources/reactions :angry)}
                 {:id 2 :label "NFT"     :emoji (resources/reactions :love)}
                 {:id 3 :label "DeFi"    :emoji (resources/reactions :thumbs-up)}
                 {:id 4 :label "NFT"     :emoji (resources/reactions :laugh)}]]
    [react/scroll-view {:horizontal                        true
                        :shows-horizontal-scroll-indicator false
                        :scroll-event-throttle             64
                        :margin-top                        12
                        :margin-bottom                     20
                        :padding-horizontal                20}
     [react/view {:flex-direction :row}
      [react/view {:margin-right  12}
       [filter-tag/tag {:icon           :main-icons2/search
                        :with-label     false
                        :type           :icon
                        :icon-color     (quo2.colors/theme-colors
                                         quo2.colors/black
                                         quo2.colors/white)}]]
      [filter-tags/tags {:data          filters
                         :with-label    true
                         :type          :emoji}]]]))

(defn views []
  (let [multiaccount @(re-frame/subscribe [:multiaccount])]
    (fn []
      [safe-area/consumer
       (fn []
         [react/view {:style {:flex             1
                              :background-color (quo2.colors/theme-colors
                                                 quo2.colors/neutral-5
                                                 quo2.colors/neutral-95)}}
          [topbar/topbar
           {:navigation      :none
            :left-component  [react/view {:margin-left 20}
                              [photos/photo (multiaccounts/displayed-photo multiaccount)
                               {:size 32}]]
            :right-component [react/view {:flex-direction :row
                                          :margin-right 20}
                              [qr-scanner]
                              [qr-code]
                              [notifications-button]]}]
          [title-column]
          [react/scroll-view
           [community-filter-tags]
           [featured-communities-section]
           [react/view {:margin-vertical    4
                        :padding-horizontal 20}
            [separator/separator]]
           [community-tabs]
           [community-tabs-view]]])])))