(ns status-im.ui.screens.views
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.utils.platform :refer [android?]]
            [status-im.utils.universal-links.core :as utils.universal-links]
            [status-im.ui.components.react :refer [view modal create-main-screen-view] :as react]
            [status-im.ui.components.styles :as common-styles]
            [status-im.ui.screens.main-tabs.views :as main-tabs]

            [status-im.ui.screens.accounts.login.views :refer [login]]
            [status-im.ui.screens.accounts.recover.views :refer [recover]]
            [status-im.ui.screens.accounts.views :refer [accounts]]

            [status-im.ui.screens.progress.views :refer [progress]]

            [status-im.ui.screens.chat.views :refer [chat chat-modal]]
            [status-im.ui.screens.add-new.views :refer [add-new]]
            [status-im.ui.screens.add-new.new-chat.views :refer [new-chat]]
            [status-im.ui.screens.add-new.new-public-chat.view :refer [new-public-chat]]

            [status-im.ui.screens.qr-scanner.views :refer [qr-scanner]]

            [status-im.ui.screens.group.views :refer [new-group]]
            [status-im.ui.screens.group.add-contacts.views :refer [contact-toggle-list
                                                                   add-participants-toggle-list]]
            [status-im.ui.screens.profile.user.views :as profile.user]
            [status-im.ui.screens.profile.contact.views :as profile.contact]
            [status-im.ui.screens.profile.group-chat.views :as profile.group-chat]
            [status-im.ui.screens.profile.photo-capture.views :refer [profile-photo-capture]]
            [status-im.ui.screens.wallet.main.views :as wallet.main]
            [status-im.ui.screens.wallet.collectibles.views :refer [collectibles-list]]
            [status-im.ui.screens.wallet.send.views :refer [send-transaction send-transaction-modal sign-message-modal]]
            [status-im.ui.screens.wallet.choose-recipient.views :refer [choose-recipient]]
            [status-im.ui.screens.wallet.request.views :refer [request-transaction send-transaction-request]]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.ui.screens.wallet.onboarding.setup.views :as wallet.onboarding.setup]
            [status-im.ui.screens.wallet.transaction-fee.views :as wallet.transaction-fee]
            [status-im.ui.screens.wallet.settings.views :as wallet-settings]
            [status-im.ui.screens.wallet.transactions.views :as wallet-transactions]
            [status-im.ui.screens.wallet.transaction-sent.views :refer [transaction-sent transaction-sent-modal]]
            [status-im.ui.screens.wallet.components.views :refer [contact-code recent-recipients recipient-qr-code]]
            [status-im.ui.screens.network-settings.views :refer [network-settings]]
            [status-im.ui.screens.network-settings.network-details.views :refer [network-details]]
            [status-im.ui.screens.network-settings.edit-network.views :refer [edit-network]]
            [status-im.ui.screens.extensions.views :refer [extensions-settings]]
            [status-im.ui.screens.log-level-settings.views :refer [log-level-settings]]
            [status-im.ui.screens.fleet-settings.views :refer [fleet-settings]]
            [status-im.ui.screens.offline-messaging-settings.views :refer [offline-messaging-settings]]
            [status-im.ui.screens.offline-messaging-settings.edit-mailserver.views :refer [edit-mailserver]]
            [status-im.ui.screens.extensions.add.views :refer [edit-extension show-extension]]
            [status-im.ui.screens.bootnodes-settings.views :refer [bootnodes-settings]]
            [status-im.ui.screens.bootnodes-settings.edit-bootnode.views :refer [edit-bootnode]]
            [status-im.ui.screens.currency-settings.views :refer [currency-settings]]
            [status-im.ui.screens.help-center.views :refer [help-center]]
            [status-im.ui.screens.browser.views :refer [browser]]
            [status-im.ui.screens.add-new.open-dapp.views :refer [open-dapp dapp-description]]
            [status-im.ui.screens.intro.views :refer [intro]]
            [status-im.ui.screens.accounts.create.views :refer [create-account]]
            [status-im.ui.screens.hardwallet.authentication-method.views :refer [hardwallet-authentication-method]]
            [status-im.ui.screens.hardwallet.connect.views :refer [hardwallet-connect]]
            [status-im.ui.screens.hardwallet.setup.views :refer [hardwallet-setup]]
            [status-im.ui.screens.hardwallet.success.views :refer [hardwallet-success]]
            [status-im.ui.screens.profile.seed.views :refer [backup-seed]]
            [status-im.ui.screens.about-app.views :as about-app]
            [status-im.utils.navigation :as navigation]
            [reagent.core :as reagent]
            [cljs-react-navigation.reagent :as nav-reagent]
            [status-im.utils.random :as rand]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.platform :as platform]
            [status-im.utils.config :as config]))

(defn wrap [view-id component]
  (fn []
    (let [main-view (create-main-screen-view view-id)]
      [main-view common-styles/flex
       [component]
       [:> navigation/navigation-events
        {:on-will-focus
         (fn []
           (log/debug :on-will-focus view-id)
           (re-frame/dispatch [:set :view-id view-id]))}]])))

(defn wrap-modal [modal-view component]
  (fn []
    (if platform/android?
      [view common-styles/modal
       [modal {:transparent      true
               :animation-type   :slide
               :on-request-close (fn []
                                   (cond
                                     (#{:wallet-send-transaction-modal
                                        :wallet-sign-message-modal}
                                      modal-view)
                                     (dispatch [:wallet/discard-transaction-navigate-back])

                                     :else
                                     (dispatch [:navigate-back])))}
        [react/main-screen-modal-view modal-view
         [component]]]]
      [react/main-screen-modal-view modal-view
       [component]])))

(defn stack-screens [screens-map]
  (->> screens-map
       (map (fn [[k v]]
              (let [screen (cond
                             (map? v)
                             (let [{:keys [screens config]} v]
                               (nav-reagent/stack-navigator
                                (stack-screens screens)
                                config))

                             (vector? v)
                             (let [[_ screen] v]
                               (nav-reagent/stack-screen
                                (wrap-modal k screen)))

                             :else
                             (nav-reagent/stack-screen (wrap k v)))]
                [k {:screen screen}])))
       (into {})))

(defn get-main-component2 [view-id]
  (log/debug :component2 view-id)
  (nav-reagent/switch-navigator
   {:intro-login-stack
    {:screen
     (nav-reagent/stack-navigator
      (stack-screens
       (cond-> {:login          login
                :progress       progress
                :create-account create-account
                :recover        recover
                :accounts       accounts}
         (= :intro view-id)
         (assoc :intro intro)

         config/hardwallet-enabled?
         (assoc :hardwallet-authentication-method hardwallet-authentication-method
                :hardwallet-connect hardwallet-connect
                :hardwallet-setup hardwallet-setup
                :hardwallet-success hardwallet-success)))
      (cond-> {:headerMode "none"}
        (#{:intro :login} view-id)
        (assoc :initialRouteName (name view-id))))}
    :chat-stack
    {:screen
     (nav-reagent/stack-navigator
      (stack-screens
       {:main-stack
        {:screens
         {:home                         (main-tabs/get-main-tab :home)
          :chat                         chat
          :profile                      profile.contact/profile
          :new                          add-new
          :new-chat                     new-chat
          :qr-scanner                   qr-scanner
          :new-group                    new-group
          :add-participants-toggle-list add-participants-toggle-list
          :contact-toggle-list          contact-toggle-list
          :group-chat-profile           profile.group-chat/group-chat-profile
          :new-public-chat              new-public-chat
          :open-dapp                    open-dapp
          :dapp-description             dapp-description
          :browser                      browser}
         :config
         {:headerMode       "none"
          :initialRouteName "home"}}

        :wallet-modal
        (wrap-modal :wallet-modal wallet.main/wallet-modal)

        :chat-modal
        (wrap-modal :chat-modal chat-modal)

        :wallet-send-modal-stack
        {:screens
         {:wallet-send-transaction-modal
          [:modal send-transaction-modal]

          :wallet-transaction-sent
          [:modal transaction-sent-modal]

          :wallet-transaction-fee
          [:modal wallet.transaction-fee/transaction-fee]}
         :config
         {:headerMode       "none"
          :initialRouteName "wallet-send-transaction-modal"}}

        :wallet-send-modal-stack-with-onboarding
        {:screens
         {:wallet-onboarding-setup-modal
          [:modal wallet.onboarding.setup/modal]

          :wallet-send-transaction-modal
          [:modal send-transaction-modal]

          :wallet-transaction-sent
          [:modal transaction-sent-modal]

          :wallet-transaction-fee
          [:modal wallet.transaction-fee/transaction-fee]}
         :config
         {:headerMode       "none"
          :initialRouteName "wallet-onboarding-setup-modal"}}

        :wallet-sign-message-modal
        [:modal sign-message-modal]})
      {:mode             "modal"
       :headerMode       "none"
       :initialRouteName "main-stack"})}
    :wallet-stack
    {:screen
     (nav-reagent/stack-navigator
      {:main-stack
       {:screen
        (nav-reagent/stack-navigator
         (stack-screens
          {:wallet                       (main-tabs/get-main-tab :wallet)
           :collectibles-list            collectibles-list
           :wallet-onboarding-setup      wallet.onboarding.setup/screen
           :wallet-send-transaction-chat send-transaction
           :contact-code                 contact-code
           :send-transaction-stack       {:screens {:wallet-send-transaction send-transaction
                                                    :recent-recipients       recent-recipients
                                                    :wallet-transaction-sent transaction-sent
                                                    :recipient-qr-code       recipient-qr-code
                                                    :wallet-send-assets      wallet.components/send-assets}
                                          :config  {:headerMode "none"}}

           :request-transaction-stack    {:screens {:wallet-request-transaction      request-transaction
                                                    :wallet-send-transaction-request send-transaction-request
                                                    :wallet-request-assets           wallet.components/request-assets
                                                    :recent-recipients               recent-recipients}
                                          :config  {:headerMode "none"}}
           :unsigned-transactions        wallet-transactions/transactions
           :transactions-history         wallet-transactions/transactions
           :wallet-transaction-details   wallet-transactions/transaction-details})
         {:headerMode       "none"
          :initialRouteName "wallet"})}
       :wallet-settings-assets
       {:screen (nav-reagent/stack-screen
                 (wrap-modal :wallet-settings-assets wallet-settings/manage-assets))}

       :wallet-transaction-fee
       {:screen (nav-reagent/stack-screen
                 (wrap-modal :wallet-transaction-fee
                             wallet.transaction-fee/transaction-fee))}

       :wallet-transactions-filter
       {:screen (nav-reagent/stack-screen
                 (wrap-modal :wallet-transactions-filter
                             wallet-transactions/filter-history))}}

      {:mode             "modal"
       :headerMode       "none"
       :initialRouteName "main-stack"})}
    :profile-stack
    {:screen
     (nav-reagent/stack-navigator
      {:main-stack
       {:screen
        (nav-reagent/stack-navigator
         (stack-screens
          (cond-> {:my-profile                       (main-tabs/get-main-tab :my-profile)
                   :profile-photo-capture            profile-photo-capture
                   :about-app                        about-app/about-app
                   :bootnodes-settings               bootnodes-settings
                   :edit-bootnode                    edit-bootnode
                   :offline-messaging-settings       offline-messaging-settings
                   :edit-mailserver                  edit-mailserver
                   :help-center                      help-center
                   :extensions-settings              extensions-settings
                   :edit-extension                   edit-extension
                   :show-extension                   show-extension
                   :network-settings                 network-settings
                   :network-details                  network-details
                   :edit-network                     edit-network
                   :log-level-settings               log-level-settings
                   :fleet-settings                   fleet-settings
                   :currency-settings                currency-settings
                   :backup-seed                      backup-seed
                   :login                            login
                   :create-account                   create-account
                   :recover                          recover
                   :accounts                         accounts
                   :qr-scanner                       qr-scanner}

            config/hardwallet-enabled?
            (assoc :hardwallet-authentication-method hardwallet-authentication-method
                   :hardwallet-connect hardwallet-connect
                   :hardwallet-setup hardwallet-setup
                   :hardwallet-success hardwallet-success)))
         {:headerMode       "none"
          :initialRouteName "my-profile"})}
       :profile-qr-viewer
       {:screen (nav-reagent/stack-screen (wrap-modal :profile-qr-viewer profile.user/qr-viewer))}}
      {:mode             "modal"
       :headerMode       "none"
       :initialRouteName "main-stack"})}}
   {:initialRouteName (if (= view-id :home)
                        "chat-stack"
                        "intro-login-stack")}))

(defn get-main-component [view-id]
  (case view-id
    :new-group new-group
    :add-participants-toggle-list add-participants-toggle-list
    :contact-toggle-list contact-toggle-list
    :group-chat-profile profile.group-chat/group-chat-profile
    :contact-code contact-code
    [react/view [react/text (str "Unknown view: " view-id)]]))

(defonce rand-label (rand/id))

(defonce initial-view-id (atom nil))

(defn main []
  (let [view-id        (re-frame/subscribe [:get :view-id])
        main-component (atom nil)]
    (reagent/create-class
     {:component-did-mount
      (fn []
        (log/debug :main-component-did-mount @view-id)
        (utils.universal-links/initialize))
      :component-will-mount
      (fn []
        (when-not @initial-view-id
          (reset! initial-view-id @view-id))
        (when (and @initial-view-id
                   (or
                    js/goog.DEBUG
                    (not @main-component)))
          (reset! main-component (get-main-component2
                                  (if js/goog.DEBUG
                                    @initial-view-id
                                    @view-id)))))
      :component-will-unmount
      utils.universal-links/finalize
      :component-will-update
      (fn []
        (when-not @initial-view-id
          (reset! initial-view-id @view-id))
        (when (and @initial-view-id (not @main-component))
          (reset! main-component (get-main-component2
                                  (if js/goog.DEBUG
                                    @initial-view-id
                                    @view-id))))
        (react/dismiss-keyboard!))
      :component-did-update
      (fn []
        (log/debug :main-component-did-update @view-id))
      :reagent-render
      (fn []
        (when (and @view-id main-component)
          [:> @main-component
           {:ref            (fn [r]
                              (navigation/set-navigator-ref r)
                              (when (and
                                     platform/android?
                                     (not js/goog.DEBUG)
                                     (not (contains? #{:intro :login} @view-id)))
                                (navigation/navigate-to @view-id)))
            ;; see https://reactnavigation.org/docs/en/state-persistence.html#development-mode
            :persistenceKey (when js/goog.DEBUG rand-label)}]))})))
