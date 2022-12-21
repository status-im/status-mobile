(ns status-im.ui.screens.wallet-connect.session-proposal.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.platform :as platform]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.constants :as constants]
            [i18n.i18n :as i18n]
            [status-im.ui.components.bottom-panel.views :as bottom-panel]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet-connect.session-proposal.styles :as styles]
            [status-im.utils.utils :as status.utils]
            [utils.security.core]))

(def chevron-icon-container-width 24)

(def chevron-icon-container-height 24)

(defn toolbar-selection
  [{:keys [text background-color on-press]}]
  [react/touchable-opacity {:on-press on-press}
   [react/view (styles/toolbar-container background-color)
    [quo/text
     {:color  :inverse
      :weight :medium
      :style  styles/toolbar-text}
     text]
    [icons/icon
     :main-icons/chevron-down
     {:color  (:text-05 @colors/theme)
      :width  chevron-icon-container-width
      :height chevron-icon-container-height}]]])

(def show-account-selector? (reagent/atom false))

(defn render-account
  [{:keys [address name color] :as account} _ _ {:keys [selected-account on-select]}]
  (let [account-selected? (= (:address @selected-account) address)]
    [react/touchable-without-feedback
     {:on-press #(do
                   (reset! selected-account (merge {} account))
                   (when on-select (on-select)))}
     [react/view (styles/account-container color account-selected?)
      [quo/text
       {:color  :inverse
        :weight (if account-selected? :medium :regular)}
       name]]]))

(defn account-selector
  [accounts selected-account-atom on-select]
  [react/view styles/account-selector-container
   [quo/text {:size :small} (i18n/label :t/select-account)]
   [list/flat-list
    {:data                              accounts
     :key-fn                            :address
     :render-fn                         render-account
     :render-data                       {:selected-account selected-account-atom
                                         :on-select        on-select}
     :horizontal                        true
     :shows-horizontal-scroll-indicator false
     :extra-data                        @selected-account-atom
     :style                             styles/account-selector-list}]])

(defn account-picker
  [accounts selected-account-atom {:keys [on-press on-select]}]
  (if (> (count accounts) 1)
    [react/view {:style styles/account-selector-wrapper}
     [account-selector accounts selected-account-atom on-select]]
    [react/touchable-opacity {:style styles/single-account-container}
     [toolbar-selection
      {:text             (:name @selected-account-atom)
       :background-color (:color @selected-account-atom)
       :on-press         on-press}]]))

(defview success-sheet-view
  [{:keys [wc-version] :as session-data}]
  (letsubs [visible-accounts [:visible-accounts-without-watch-only]
            dapps-account    [:dapps-account]
            sessions         [:wallet-connect/sessions]
            sessions-legacy  [:wallet-connect-legacy/sessions]
            managed-session  [:wallet-connect/session-managed]]
    (let [{:keys [topic]}                  (when-not (= wc-version constants/wallet-connect-version-1)
                                             session-data)
          peerId                           (when (= wc-version constants/wallet-connect-version-1)
                                             (get-in session-data [:params 0 :peerId]))
          session-legacy                   (when (= wc-version constants/wallet-connect-version-1)
                                             (first (filter #(= (get-in % [:params 0 :peerId]) peerId)
                                                            sessions-legacy)))
          {:keys [peer state] :as session} (first (filter #(= (:topic %) topic) sessions))
          {:keys [params]}                 (when (= wc-version constants/wallet-connect-version-1)
                                             session-legacy)
          {:keys [metadata]}               peer
          {:keys [peerMeta]}               (first params)
          {:keys [accounts]}               (if (= wc-version constants/wallet-connect-version-1)
                                             (first params)
                                             state)
          {:keys [name icons]}             (if (= wc-version constants/wallet-connect-version-1)
                                             peerMeta
                                             metadata)
          filtered-icons                   (status.utils/exclude-svg-resources icons)
          icon-uri                         (when (and filtered-icons (pos? (count filtered-icons)))
                                             (first filtered-icons))
          address                          (if (= wc-version constants/wallet-connect-version-1)
                                             (first accounts)
                                             (last (string/split (first accounts) #":")))
          account                          (first (filter #(= (:address %) address) visible-accounts))
          selected-account-atom            (reagent/atom account)]
      [react/view (styles/proposal-sheet-container)
       [react/view (styles/proposal-sheet-header)
        [quo/text
         {:weight :bold
          :size   :large}
         (i18n/label :t/successful-connection)]]
       [react/image
        {:style  (styles/dapp-logo)
         :source {:uri icon-uri}}]
       [react/view styles/sheet-body-container
        [react/view styles/proposal-title-container
         [quo/text
          {:weight :bold
           :size   :large}
          name]
         [quo/text
          {:weight :regular
           :size   :large}
          (i18n/label :t/connected)]]]
       [account-picker
        (vector dapps-account)
        selected-account-atom
        {:on-press #(do
                      (re-frame/dispatch (if (= wc-version constants/wallet-connect-version-1)
                                           [:wallet-connect-legacy/manage-app session-data]
                                           [:wallet-connect/manage-app session]))
                      (reset! show-account-selector? true))}]
       [quo/text
        {:weight :regular
         :color  :secondary
         :style  styles/message-title}
        (i18n/label :t/manage-connections)]
       [react/view (styles/success-button-container)
        [quo/button
         {:theme    :accent
          :on-press #(do
                       (reset! show-account-selector? false)
                       (re-frame/dispatch [:hide-wallet-connect-success-sheet]))}
         (i18n/label :t/close)]]
       (when managed-session
         (if platform/ios?
           [react/blur-view
            {:style      (styles/blur-view)
             :blurAmount 2
             :blurType   (if (colors/dark?) :dark :light)}]
           [react/view (styles/blur-view)]))])))

(defview app-management-sheet-view
  [{:keys [wc-version] :as session}]
  (letsubs [sessions         [:wallet-connect/sessions]
            sessions-legacy  [:wallet-connect-legacy/sessions]
            visible-accounts [:visible-accounts-without-watch-only]]
    (let [peerId                   (when (= wc-version constants/wallet-connect-version-1)
                                     (get-in session [:params 0 :peerId]))
          session-legacy           (when (= wc-version constants/wallet-connect-version-1)
                                     (first (filter #(= (get-in % [:params 0 :peerId]) peerId)
                                                    sessions-legacy)))
          {:keys [topic]}          (when-not (= wc-version constants/wallet-connect-version-1) session)
          {:keys [peer state]}     (first (filter #(= (:topic %) topic) sessions))
          {:keys [params]}         (when (= wc-version constants/wallet-connect-version-1)
                                     session-legacy)
          {:keys [metadata]}       peer
          {:keys [peerMeta]}       (first params)
          {:keys [accounts]}       (if (= wc-version constants/wallet-connect-version-1)
                                     (first params)
                                     state)
          {:keys [name icons url]} (if (= wc-version constants/wallet-connect-version-1)
                                     peerMeta
                                     metadata)
          filtered-icons           (status-im.utils.utils/exclude-svg-resources icons)
          icon-uri                 (when (and filtered-icons (pos? (count filtered-icons)))
                                     (first filtered-icons))
          address                  (if (= wc-version constants/wallet-connect-version-1)
                                     (first accounts)
                                     (last (string/split (first accounts) #":")))
          account                  (first (filter #(= (:address %) address) visible-accounts))
          selected-account-atom    (reagent/atom account)]
      [react/view {:style (merge (styles/acc-sheet) {:background-color "rgba(0,0,0,0)"})}
       [react/linear-gradient
        {:colors ["rgba(0,0,0,0)" "rgba(0,0,0,0.3)"]
         :start  {:x 0 :y 0}
         :end    {:x 0 :y 1}
         :style  styles/shadow}]
       [react/view (styles/proposal-sheet-container)
        [react/view (styles/management-sheet-header)
         [react/image
          {:style  styles/management-icon
           :source {:uri icon-uri}}]
         [react/view styles/app-info-container
          [quo/text {:weight :medium} name]
          [quo/text
           {:color           :secondary
            :number-of-lines 1
            :elipsize-mode   :tail} url]]
         [quo/button
          {:type     :secondary
           :theme    :secondary
           :on-press #(re-frame/dispatch (if (= wc-version constants/wallet-connect-version-1)
                                           [:wallet-connect-legacy/disconnect session-legacy]
                                           [:wallet-connect/disconnect topic]))}
          (i18n/label :t/disconnect)]]
        [account-selector
         visible-accounts
         selected-account-atom
         #(re-frame/dispatch
           (if (= wc-version constants/wallet-connect-version-1)
             [:wallet-connect-legacy/change-session-account session-legacy @selected-account-atom]
             [:wallet-connect/change-session-account topic @selected-account-atom]))]]])))

(defview session-proposal-sheet
  [{:keys [name icons wc-version]}]
  (letsubs [visible-accounts [:visible-accounts-without-watch-only]
            dapps-account    [:dapps-account]]
    (let [filtered-icons        (status-im.utils.utils/exclude-svg-resources icons)
          icon-uri              (when (and filtered-icons (> (count filtered-icons) 0))
                                  (first filtered-icons))
          selected-account-atom (reagent/atom dapps-account)]
      [react/view (styles/proposal-sheet-container)
       [react/view (styles/proposal-sheet-header)
        [quo/text
         {:weight :bold
          :size   :large}
         (i18n/label :t/connection-request)]]
       [react/image
        {:style  (styles/dapp-logo)
         :source {:uri icon-uri}}]
       [react/view styles/sheet-body-container
        [react/view styles/proposal-title-container
         [quo/text
          {:weight :bold
           :size   :large}
          (str name " ")]
         [quo/text
          {:weight :regular
           :size   :large}
          (i18n/label :t/wallet-connect-proposal-title)]]]
       [account-picker visible-accounts selected-account-atom]
       [react/view
        (merge (styles/proposal-buttons-container)
               (when (= (count visible-accounts) 1) {:margin-top 12}))
        [quo/button
         {:type     :secondary
          :on-press #(if (= wc-version constants/wallet-connect-version-1)
                       (re-frame/dispatch [:wallet-connect-legacy/reject-proposal])
                       (re-frame/dispatch [:wallet-connect/reject-proposal]))}
         (i18n/label :t/reject)]
        [quo/button
         {:theme    :accent
          :on-press #(if (= wc-version constants/wallet-connect-version-1)
                       (re-frame/dispatch [:wallet-connect-legacy/approve-proposal
                                           @selected-account-atom])
                       (re-frame/dispatch [:wallet-connect/approve-proposal @selected-account-atom]))}
         (i18n/label :t/connect)]]])))

(defview wallet-connect-proposal-sheet
  []
  (letsubs [proposal-metadata [:wallet-connect/proposal-metadata]]
    [bottom-panel/animated-bottom-panel
     proposal-metadata
     session-proposal-sheet
     #(re-frame/dispatch [:hide-wallet-connect-sheet])]))

(defview wallet-connect-success-sheet-view
  []
  (letsubs [session [:wallet-connect/session-connected]]
    [bottom-panel/animated-bottom-panel
     session
     success-sheet-view
     #(re-frame/dispatch [:hide-wallet-connect-success-sheet])]))

(defview wallet-connect-app-management-sheet-view
  []
  (letsubs [session [:wallet-connect/session-managed]]
    [bottom-panel/animated-bottom-panel
     session
     app-management-sheet-view
     #(re-frame/dispatch [:hide-wallet-connect-app-management-sheet])
     #(re-frame/dispatch [:hide-wallet-connect-app-management-sheet])
     false]))
