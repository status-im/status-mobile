(ns status-im.ui.screens.communities.communities-list-redesign
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.screens.communities.community-views-redesign :as community-views]
            [quo.components.separator :as separator]
            [quo2.components.text :as quo2.text]
            [quo2.components.button :as quo2.button]
            [quo2.components.filter-tags :as quo2.filter-tags]
            [quo2.foundations.typography :as typography]
            [quo2.foundations.colors :as quo2.colors]
            [status-im.qr-scanner.core :as qr-scanner]
            [quo2.components.tabs :as quo2.tabs]
            [status-im.constants :as constants]
            [quo.design-system.colors :as colors]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.plus-button-redesign :as components.plus-button]
            [status-im.ui.components.icons.icons :as icons])
  (:require-macros [status-im.utils.views :as views]))

(def selected-tag (reagent/atom 0))
(def selected-tab (reagent/atom :all))

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
   :main-icons2/placeholder])

(defn qr-code []
  [quo2.button/button
   {:icon                true
    :type                :grey
    :size                32
    :style               {:margin-left 10}
    :accessibility-label :contact-qr-code-button}
   :main-icons2/placeholder])

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
      :main-icons2/placeholder]
     (when (pos? notif-count)
       [react/view {:style (merge (styles/counter-public-container) {:top 5 :right 5})
                    :pointer-events :none}
        [react/view {:style               styles/counter-public
                     :accessibility-label :notifications-unread-badge}]])]))

(views/defview plus-button []
  (views/letsubs [logging-in? [:multiaccounts/login]]
    [components.plus-button/plus-button
     {:on-press (when-not logging-in?
                  #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading logging-in?
      :accessibility-label :new-chat-button}]))

(defn render-popular-fn [{:keys [chat-id] :as community-item}]
  (when-not chat-id
    [community-views/community-card-list-item community-item :popular]))

(defn render-featured-fn [{:keys [chat-id] :as community-item}]
  (when-not chat-id
    [community-views/community-card-list-item community-item :featured]))

(defn render-categorized-fn [{:keys [chat-id] :as community-item}]
  (when-not chat-id
    [community-views/categorized-communities-list-item community-item]))

(defn community-list-key-fn [item]
  (:id item))

(defn get-item-layout [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(def communities-items-featured
  {:data [{:id             constants/status-community-id
           :name           "Status"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :type           "token-gated"
           :section        "featured"
           :permissions-granted true
           :community-icon (resources/get-image :status-logo)
           :color               (rand-nth colors/chat-colors)
           :tags [{:id 1 :label "Crypto" :resource (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :resource (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :resource (resources/reactions :thumbs-up)}]}
          {:id             2
           :name           "Status"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :type           "open"
           :section        "featured"
           :community-icon (resources/get-image :status-logo)
           :color               (rand-nth colors/chat-colors)
           :tags [{:id 1 :label "Crypto" :resource (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :resource (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :resource (resources/reactions :thumbs-up)}]}]})

(def communities-items-popular
  {:data [{:id             constants/status-community-id
           :name           "Status"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :type           "token-gated"
           :section        "popular"
           :permissions-granted true
           :community-icon      (resources/get-image :status-logo)
           :color               (rand-nth colors/chat-colors)
           :tags [{:id 1 :label "Crypto" :resource (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :resource (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :resource (resources/reactions :thumbs-up)}]}
          {:id             2
           :name           "Status"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :type           "open"
           :section        "popular"
           :community-icon (resources/get-image :status-logo)
           :color               (rand-nth colors/chat-colors)
           :tags [{:id 1 :label "Crypto" :resource (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :resource (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :resource (resources/reactions :thumbs-up)}]}]})

(def all-communities-items
  {:data [{:id             constants/status-community-id
           :name           "Status"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :type           "token-gated"
           :section        "all"
           :permissions-granted true
           :community-icon (resources/get-image :status-logo)
           :color               (rand-nth colors/chat-colors)
           :tags [{:id 1 :label "Crypto" :resource (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :resource (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :resource (resources/reactions :thumbs-up)}]}
          {:id             2
           :name           "Status"
           :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
           :type           "open"
           :section        "all"
           :community-icon (resources/get-image :status-logo)
           :color               (rand-nth colors/chat-colors)
           :tags [{:id 1 :label "Crypto" :resource (resources/reactions :angry)}
                  {:id 2 :label "NFT"    :resource (resources/reactions :love)}
                  {:id 3 :label "DeFi"   :resource (resources/reactions :thumbs-up)}]}]})

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

(views/defview all-communities []
  (let [items (get all-communities-items :data)]
    [list/flat-list
     {:key-fn                            community-list-key-fn
      :getItemLayout                     get-item-layout
      :keyboard-should-persist-taps      :always
      :shows-horizontal-scroll-indicator false
      :data                              items
      :render-fn                         render-categorized-fn}]))

(views/defview popular-communities []
  (let [items (get communities-items-popular :data)]
    [list/flat-list
     {:key-fn                            community-list-key-fn
      :getItemLayout                     get-item-layout
      :keyboard-should-persist-taps      :always
      :shows-horizontal-scroll-indicator false
      :data                              items
      :render-fn                         render-popular-fn}]))

(defn community-tabs []
  (let [tab @selected-tab]
    [react/view {:margin-top          8
                 :margin-bottom       4
                 :padding-horizontal  20}
     [react/view {:padding-vertical   12}
      [quo2.tabs/tabs {:size              32
                       :on-change         #(reset! selected-tab %)
                       :default-active    :all
                       :data [{:id :all   :label "All"}
                              {:id :open  :label "Open"}
                              {:id :gated :label "Token Gated"}]}]]
     (cond
       (= tab :all)
       [react/view
        [all-communities]]
       (= tab :open)
       [react/view
        [all-communities]]
       (= tab :gated)
       [react/view
        [all-communities]])]))

(defn communities-sections []
  [:<>
   [react/view {:padding-left 20}
    [react/view {:flex-direction      :row
                 :align-items         :center
                 :padding-bottom      8}
     [quo2.text/text
      {:style (merge {:accessibility-label :featured-communities-title}
                     typography/paragraph-1
                     typography/font-semi-bold)}
      "Featured"]]
    [featured-communities]]
   [react/view {:margin-vertical    20
                :padding-horizontal 20}
    [separator/separator-redesign]]
   [react/view {:padding-horizontal 20}
    [quo2.text/text
     {:style (merge {:accessibility-label :popular-communities-title
                     :padding-bottom      8}
                    typography/paragraph-1
                    typography/font-semi-bold)}
     "Popular"]
    [popular-communities]]
   [community-tabs]])

(defn title-column []
  [react/view
   {:flex-direction     :row
    :align-items        :center
    :justify-content    :center
    :padding-horizontal 20
    :padding-vertical   8}
   [react/view
    {:flex-direction :row
     :flex           1
     :padding-right  16
     :align-items    :center}
    [quo2.text/text
     {:style (merge {:accessibility-label :community-name-text
                     :ellipsize-mode      :tail
                     :number-of-lines     1}
                    typography/font-semi-bold
                    typography/heading-1)}
     :Communities]]
   [plus-button]])

(views/defview community-filter-tags []
  (let [filters [{:id 1 :label "Crypto"  :resource (resources/reactions :angry)}
                 {:id 2 :label "NFT"     :resource (resources/reactions :love)}
                 {:id 3 :label "DeFi"    :resource (resources/reactions :thumbs-up)}
                 {:id 2 :label "NFT"     :resource (resources/reactions :laugh)}]
        tags (for [tag filters]
               {:label (:label tag)
                :id    (:id    tag)
                :after (:after tag)})]
    [react/scroll-view {:horizontal                        true
                        :shows-horizontal-scroll-indicator false
                        :scroll-event-throttle             64
                        :padding-horizontal                20
                        :margin-top                        12
                        :margin-bottom                     20}
     [react/view {:flex-direction :row}
      [react/view {:margin-right       12
                   :height             32
                   :width              32
                   :border-radius      32
                   :border-width       1
                   :align-items        :center
                   :justify-content    :center
                   :border-color  (quo2.colors/theme-colors
                                   quo2.colors/neutral-30
                                   quo2.colors/neutral-70)}
       [icons/icon :main-icons2/search20 {:height 20
                                          :color  (quo2.colors/theme-colors
                                                   quo2.colors/black
                                                   quo2.colors/white)
                                          :width  20}]]
      [quo2.filter-tags/tags {:default-active (:id (first tags))
                              :on-change      #(reset! selected-tag %)
                              :data           filters}]]]))

(defn views []
  (fn [insets]
    [react/view {:style {:flex             1
                         :padding-top      (:top insets)
                         :background-color (quo2.colors/theme-colors quo2.colors/neutral-5 quo2.colors/neutral-95)}}
     [react/view
      {:flex-direction     :row
       :padding-horizontal 20
       :padding-vertical   12
       :align-items        :center
       :justify-content    :flex-end}
      [qr-scanner]
      [qr-code]
      [notifications-button]]
     [react/scroll-view
      [title-column]
      [community-filter-tags]
      [communities-sections]]]))