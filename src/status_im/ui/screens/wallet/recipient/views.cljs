(ns status-im.ui.screens.wallet.recipient.views
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.stateofus :as stateofus]
            [utils.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.search-input.view :as search-input]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.utils.utils :as utils]
            [utils.debounce :as debounce])
  (:require-macros [status-im.utils.views :as views]))

(defn- recipient-topbar
  []
  [topbar/topbar
   {:navigation        {:on-press
                        #(do
                           (re-frame/dispatch [:wallet/recipient-modal-closed])
                           (re-frame/dispatch [:search/recipient-filter-changed nil])
                           (re-frame/dispatch [:navigate-back]))}
    :modal?            true
    :border-bottom     false
    :title             (i18n/label :t/recipient)
    :right-accessories
    [{:icon                :qr
      :accessibility-label :scan-contact-code-button
      :on-press            #(re-frame/dispatch [:wallet.send/qr-scanner
                                                {:ignore-url true
                                                 :handler    :wallet.send/qr-scanner-result}])}]}])

(defonce search-active? (reagent/atom false))

(defn search-input-wrapper
  []
  (let [search-filter @(re-frame/subscribe [:search/recipient-filter])]
    [react/view
     {:padding-horizontal 16
      :padding-vertical   10}
     [search-input/search-input-old
      {:search-active? search-active?
       :search-filter  search-filter
       :on-cancel      #(re-frame/dispatch [:search/recipient-filter-changed nil])
       :on-change      (fn [text]
                         (re-frame/dispatch [:search/recipient-filter-changed text])
                         (re-frame/dispatch [:set-in [:contacts/new-identity :state] :searching])
                         (debounce/debounce-and-dispatch [:new-chat/set-new-identity text] 300))}]]))

(defn section
  [_ _ _]
  (let [opened? (reagent/atom false)]
    (fn [title cnt content]
      [react/view {:padding-vertical 8}
       [quo/list-item
        {:title     title
         :on-press  #(swap! opened? not)
         :accessory
         [react/view {:flex-direction :row :align-items :center}
          (when (pos? cnt)
            [react/text {:style {:color colors/gray}} cnt])
          [icons/icon (if @opened? :main-icons/dropdown :main-icons/next)
           {:container-style {:align-items     :center
                              :margin-left     8
                              :justify-content :center}
            :resize-mode     :center
            :color           colors/black}]]}]
       (when @opened?
         content)])))

(defn render-account
  [account]
  [quo/list-item
   {:icon     [chat-icon/custom-icon-view-list (:name account) (:color account)]
    :title    (:name account)
    :on-press #(re-frame/dispatch [:wallet.send/set-recipient (:address account)])
    :subtitle [quo/text
               {:monospace true
                :color     :secondary}
               (utils/get-shortened-checksum-address (:address account))]}])

(def scroll-view-ref (atom nil))

(defn contacts-list-item
  [{:keys [name] :as contact}]
  (let [[first-name second-name] (multiaccounts/contact-two-names contact true)]
    [quo/list-item
     {:title    first-name
      :subtitle second-name
      :on-press #(do
                   (some-> ^js @scroll-view-ref
                           (.scrollTo #js {:x 0 :animated true}))
                   (re-frame/dispatch [:wallet.recipient/address-changed name]))
      :icon     [chat-icon/contact-icon-contacts-tab
                 (multiaccounts/displayed-photo contact)]}]))

(defn empty-items
  [icon title]
  [react/view {:height 94 :align-items :center :justify-content :center}
   [icons/icon icon
    {:color colors/gray}]
   [react/text {:style {:color colors/gray :margin-top 8}}
    title]])

(views/defview accounts-section
  []
  (views/letsubs [accounts [:accounts-for-recipient]]
    (let [cnt (count accounts)]
      [section
       (i18n/label :t/my-accounts)
       cnt
       (if (> cnt 0)
         [react/view
          (for [account accounts]
            [render-account account])]
         [empty-items :main-icons/address (i18n/label :t/my-accounts-empty)])])))

(defn render-recent
  [{:keys [from to type amount-text currency-text]}]
  (let [inbound? (= type :inbound)
        address  (if inbound? from to)]
    [quo/list-item
     {:title     [quo/text {:monospace true}
                  (utils/get-shortened-checksum-address address)]
      :on-press  #(re-frame/dispatch [:wallet.recipient/address-changed address])
      :size      :small
      :accessory [react/text
                  {:style {:flex-shrink 1
                           :color       colors/gray}}
                  (str (if inbound? "↓ " "↑ ") amount-text " " currency-text)]}]))

(defn recent-section
  []
  (let [{:keys [from]} @(re-frame/subscribe [:wallet/prepare-transaction])
        txs            @(re-frame/subscribe [:wallet/recipient-recent-txs (:address from)])
        cnt            (count txs)]
    [section
     (i18n/label :t/recent)
     cnt
     (if (> cnt 0)
       [react/view
        (for [tx txs]
          [render-recent tx])]
       [empty-items :main-icons/history (i18n/label :t/recent-empty)])]))

(defn render-fav
  [{:keys [address name]}]
  (let [noname?       (string/blank? name)
        short-address (utils/get-shortened-checksum-address address)]
    [quo/list-item
     {:icon     [chat-icon/custom-icon-view-list
                 (if noname? " 2" name)
                 (rand-nth colors/chat-colors)]
      :title    (if noname?
                  [quo/text {:monospace true}
                   short-address]
                  name)
      :subtitle (when-not noname?
                  [quo/text
                   {:monospace true
                    :color     :secondary}
                   short-address])
      :on-press #(re-frame/dispatch [:wallet.send/set-recipient address])
      :size     (when noname? :small)}]))

(views/defview fav-section
  []
  (views/letsubs [favourites [:wallet/favourites-filtered]]
    (let [cnt (count favourites)]
      [section
       (i18n/label :t/favourites)
       cnt
       [react/view
        ;;TODO implement later
        #_[quo/list-item
           {:title "Add favourite"
            :icon  :main-icons/add
            :theme :accent}]
        (if (> cnt 0)
          [react/view
           (for [data favourites]
             [render-fav data])]
          [empty-items :main-icons/favourite (i18n/label :t/favourites-empty)])]])))

(views/defview contacts-section
  []
  (views/letsubs [contacts [:contacts/active-with-ens-names]]
    (let [cnt (count contacts)]
      [section
       (i18n/label :t/contacts)
       cnt
       (if (> cnt 0)
         [react/view
          (for [contact contacts]
            [contacts-list-item contact])]
         [empty-items :main-icons/username (i18n/label :t/contacts-empty)])])))

(views/defview search-results
  []
  (views/letsubs [contacts   [:contacts/active-with-ens-names]
                  favourites [:wallet/favourites-filtered]
                  accounts   [:accounts-for-recipient]]
    [react/view
     (for [account accounts]
       [render-account account])
     (for [data favourites]
       [render-fav data])
     (for [contact contacts]
       [contacts-list-item contact])]))

(defn accordion
  [search-filter]
  (if (not (string/blank? search-filter))
    [search-results]
    [react/view
     [components/separator]
     [accounts-section]
     [components/separator]
     [recent-section]
     [components/separator]
     [fav-section]
     [components/separator]
     [contacts-section]
     [components/separator]]))

(views/defview new-favourite
  []
  (views/letsubs [{:keys [resolved-address]} [:wallet/recipient]
                  fav-name                   (atom "")]
    [kb-presentation/keyboard-avoiding-view {:style {:flex 1}}
     [react/view {:flex 1}
      [react/scroll-view {:style {:flex 1}}
       [react/view {:padding-horizontal 16}
        [react/view
         {:flex-direction  :row
          :justify-content :space-between
          :align-items     :center
          :height          40
          :margin-vertical 8}
         [quo/text (i18n/label :t/address-or-ens-name)]]
        [quo/text-input
         {:multiline           true
          :default-value       resolved-address
          :height              70
          :editable            false
          :accessibility-label :fav-address}]]
       [react/view {:height 16}]
       [quo/list-header (i18n/label :t/name-optional)]
       [react/view {:padding-horizontal 16}
        [quo/text-input
         {:show-cancel         false
          :accessibility-label :fav-name
          :on-change-text      #(reset! fav-name %)}]]]
      [toolbar/toolbar
       {:show-border? true
        :center
        [quo/button
         {:accessibility-label :add-fav
          :type                :secondary
          :on-press            #(re-frame/dispatch [:wallet/add-favourite resolved-address @fav-name])}
         (i18n/label :t/add)]}]]]))

(views/defview recipient
  []
  (views/letsubs [{:keys [address resolved-address searching]} [:wallet/recipient]
                  search-filter                                [:search/recipient-filter]]
    (let [disabled? (or searching (not resolved-address))]
      [kb-presentation/keyboard-avoiding-view
       {:style         {:flex 1}
        :ignore-offset true}
       [react/view {:flex 1}
        [recipient-topbar]
        [search-input-wrapper]
        [react/scroll-view
         {:style                        {:flex 1}
          :keyboard-should-persist-taps :handled
          :ref                          #(reset! scroll-view-ref %)}
         [react/view
          [components/separator]
          [react/view {:padding-horizontal 16 :margin-bottom 16}
           [react/view
            {:flex-direction  :row
             :justify-content :space-between
             :align-items     :center
             :height          40
             :margin-vertical 8}
            [quo/text (i18n/label :t/address-or-ens-name)]
            [quo/button
             {:type     :secondary
              :on-press #(re-frame/dispatch [:wallet.recipient/address-paste-pressed])}
             (i18n/label :t/paste)]]
           [quo/text-input
            {:multiline           true
             :default-value       address
             :height              70
             :placeholder         (i18n/label :t/recipient-code-placeholder)
             :text-align-vertical :top
             :on-change-text      #(do
                                     (re-frame/dispatch [:set-in [:wallet/recipient :searching]
                                                         :searching])
                                     (debounce/debounce-and-dispatch [:wallet.recipient/address-changed
                                                                      (utils/safe-trim %)]
                                                                     600))
             :accessibility-label :recipient-address-input}]]
          [react/view {:align-items :center :height 30 :padding-bottom 8}
           (if searching
             [react/small-loading-indicator]
             (when resolved-address
               [quo/text
                {:style {:margin-horizontal 16}
                 :size  :small
                 :align :center
                 :color :secondary}
                (when-not (ethereum/address? address)
                  (str (stateofus/username-with-domain address) " • "))
                [quo/text
                 {:monospace true
                  :size      :inherit
                  :color     :inherit}
                 (utils/get-shortened-address resolved-address)]]))]
          [accordion search-filter]]]
        [toolbar/toolbar
         {:show-border? true
          :left
          [quo/button
           {:accessibility-label :participant-add-to-favs
            :type                :secondary
            :disabled            disabled?
            :on-press            #(re-frame/dispatch [:open-modal :new-favourite])}
           (i18n/label :t/add-to-favourites)]
          :right
          [quo/button
           {:accessibility-label :participant-done
            :type                :secondary
            :after               :main-icons/next
            :disabled            disabled?
            :on-press            #(re-frame/dispatch [:wallet.send/set-recipient resolved-address])}
           (i18n/label :t/done)]}]]])))
