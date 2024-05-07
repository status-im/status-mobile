(ns status-im.contexts.settings.wallet.saved-addresses.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.common.resources :as resources]
            [status-im.config :as config]
            [status-im.contexts.settings.wallet.saved-addresses.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- empty-state
  []
  (let [theme (quo.theme/use-theme)]
    [quo/empty-state
     {:title           (i18n/label :t/no-saved-addresses)
      :description     (i18n/label :t/you-like-to-type-43-characters)
      :image           (resources/get-themed-image :sweating-man theme)
      :container-style style/empty-container-style}]))

(defn- option
  [{:keys [icon label on-press danger? sub-label add-divider? accessibility-label right-icon] :as opt}]
  (when opt
    ^{:key label}
    {:icon                icon
     :label               label
     :on-press            on-press
     :danger?             danger?
     :sub-label           sub-label
     :right-icon          right-icon
     :add-divider?        add-divider?
     :accessibility-label accessibility-label}))

(defn- options
  [account-name address]
  [(when config/show-not-implemented-features?
     {:icon                :i/arrow-up
      :label               (i18n/label :t/send-to-user {:user account-name})
      :on-press            not-implemented/alert
      :accessibility-label :manage-notifications})
   (when config/show-not-implemented-features?
     {:icon                :i/link
      :right-icon          :i/external
      :label               (i18n/label :t/view-address-on-website {:website "Etherscan"})
      :on-press            not-implemented/alert
      :accessibility-label :manage-notifications})
   (when config/show-not-implemented-features?
     {:icon                :i/link
      :right-icon          :i/external
      :label               (i18n/label :t/view-address-on-website {:website "Optimistic"})
      :on-press            not-implemented/alert
      :accessibility-label :manage-notifications})
   {:icon                :i/share
    :on-press            #(rf/dispatch
                           [:open-share
                            {:options (if platform/ios?
                                        {:activityItemSources [{:placeholderItem {:type    :text
                                                                                  :content address}
                                                                :item            {:default {:type :text
                                                                                            :content
                                                                                            address}}
                                                                :linkMetadata    {:title address}}]}
                                        {:title     address
                                         :subject   address
                                         :message   address
                                         :isNewTask true})}])
    :label               (i18n/label :t/share-address)
    :accessibility-label :manage-notifications}
   {:icon                :i/qr-code
    :label               (i18n/label :t/show-address-qr)
    :on-press            (fn []
                           (rf/dispatch [:wallet/set-current-viewing-account address])
                           (rf/dispatch [:open-modal :screen/wallet.share-address {:status :share}]))
    :accessibility-label :manage-notifications}
   (when config/show-not-implemented-features?
     {:icon                :i/edit
      :label               (i18n/label :t/edit-account)
      :on-press            #(rf/dispatch [:navigate-to :screen/wallet.edit-account])
      :accessibility-label :manage-notifications})
   (when config/show-not-implemented-features?
     {:icon                :i/delete
      :label               (i18n/label :t/remove-account)
      :on-press            not-implemented/alert
      :danger?             true
      :accessibility-label :manage-notifications
      :add-divider?        true})])

(defn- sample-options
  [account-name address]
  (keep option (options account-name address)))

(defn- account-sheet
  [address account-name]
  [quo/action-drawer
   [(sample-options account-name address)]])

(defn- on-press-saved-address
  [{:keys [address account-name ens-name customization-color]}]
  (rf/dispatch
   [:show-bottom-sheet
    {:selected-item (fn []
                      [quo/saved-address
                       {:active-state? false
                        :user-props    {:name                account-name
                                        :address             address
                                        :ens                 ens-name
                                        :customization-color customization-color}}])
     :content       (fn []
                      [account-sheet
                       address account-name])}]))

(defn- saved-address
  [{:keys        [address colorId _chain-short-names _test? ens?]
    account-name :name}]
  [quo/saved-address
   {:on-press   #(on-press-saved-address
                  {:account-name        account-name
                   :address             address
                   :ens-name            (when ens? address)
                   :customization-color (keyword colorId)})
    :user-props {:name                account-name
                 :address             address
                 :customization-color (keyword colorId)}}])

(defn- header
  [{:keys [title]}]
  [quo/divider-label
   {:container-style {:border-top-color colors/white-opa-5
                      :margin-top       16}}
   title])

(defn- footer
  []
  [rn/view {:height 8}])

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- view
  []
  (let [inset-top           (safe-area/get-top)
        customization-color (rf/sub [:profile/customization-color])
        saved-addresses     (rf/sub [:wallet/grouped-saved-addresses])]
    (rn/use-effect
     (fn []
       (rf/dispatch [:wallet/get-saved-addresses])))
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper inset-top)}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [rn/view {:style style/title-container}
      [quo/standard-title
       {:title               (i18n/label :t/saved-addresses)
        :accessibility-label :saved-addresses-header
        :right               :action
        :customization-color customization-color
        :icon                :i/add}]]

     [rn/section-list
      {:key-fn                         :title
       :sticky-section-headers-enabled false
       :render-section-header-fn       header
       :render-section-footer-fn       footer
       :sections                       saved-addresses
       :render-fn                      saved-address
       :separator                      [rn/view {:style {:height 4}}]}]
     (when-not (seq saved-addresses)
       [empty-state])]))
