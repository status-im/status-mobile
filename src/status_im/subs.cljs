(ns status-im.subs
  (:require [re-frame.core :refer [reg-sub subscribe] :as re-frame]
            status-im.tribute-to-talk.subs
            status-im.ui.screens.hardwallet.connect.subs
            status-im.ui.screens.hardwallet.settings.subs
            status-im.ui.screens.hardwallet.pin.subs
            status-im.ui.screens.hardwallet.setup.subs
            [cljs.spec.alpha :as spec]
            [clojure.string :as string]

            [status-im.chat.db :as chat.db]
            [status-im.accounts.db :as accounts.db]
            [status-im.contact.db :as contact.db]

            [status-im.browser.core :as browser]
            [status-im.fleet.core :as fleet]
            [status-im.constants :as constants]

            [status-im.models.transactions :as transactions]
            [status-im.models.wallet :as models.wallet]

            [status-im.chat.commands.input :as commands.input]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.constants :as chat.constants]

            [status-im.ui.components.toolbar.styles :as toolbar.styles]
            [status-im.ui.components.bottom-bar.styles :as tabs.styles]

            [status-im.ui.screens.chat.stickers.styles :as stickers.styles]
            [status-im.ui.screens.add-new.new-public-chat.db :as db]
            [status-im.ui.screens.mobile-network-settings.utils :as mobile-network-utils]
            [status-im.ui.screens.wallet.utils :as wallet.utils]

            [status-im.utils.universal-links.core :as links]
            [status-im.utils.platform :as platform]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.security :as security]
            [status-im.utils.config :as config]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.build :as build]
            [status-im.utils.hex :as utils.hex]
            [status-im.utils.datetime :as datetime]

            [status-im.i18n :as i18n]))

;; TOP LEVEL ===========================================================================================================

(def subs-keys (atom {}))

(defn reg-root-key-sub [sub-name db-key keys-key]
  (swap! subs-keys update keys-key conj db-key)
  (reg-sub sub-name :<- [keys-key] (fn [db] (get db db-key))))

;;view
(reg-root-key-sub :view-id :view-id :subs/view-keys)
(reg-root-key-sub :navigation-stack :navigation-stack :subs/view-keys)
(reg-root-key-sub :screen-params :navigation/screen-params :subs/view-keys)

;;bottom sheet
(reg-root-key-sub :bottom-sheet/show? :bottom-sheet/show? :subs/bottom-keys)
(reg-root-key-sub :bottom-sheet/view :bottom-sheet/view :subs/bottom-keys)

;;general
(reg-root-key-sub :network-name :chain :subs/general-keys)
(reg-root-key-sub :sync-state :sync-state :subs/general-keys)
(reg-root-key-sub :network-status :network-status :subs/general-keys)
(reg-root-key-sub :peers-count :peers-count :subs/general-keys)
(reg-root-key-sub :about-app/node-info :node-info :subs/general-keys)
(reg-root-key-sub :peers-summary :peers-summary :subs/general-keys)
(reg-root-key-sub :node-status :node/status :subs/general-keys)
(reg-root-key-sub :tab-bar-visible? :tab-bar-visible? :subs/general-keys)
(reg-root-key-sub :dimensions/window :dimensions/window :subs/general-keys)
(reg-root-key-sub :initial-props :initial-props :subs/general-keys)
(reg-root-key-sub :get-manage-extension :extensions/manage :subs/general-keys)
(reg-root-key-sub :get-staged-extension :extensions/staged-extension :subs/general-keys)
(reg-root-key-sub :get-device-UUID :device-UUID :subs/general-keys)
(reg-root-key-sub :fleets/custom-fleets :custom-fleets :subs/general-keys)
(reg-root-key-sub :chain-sync-state :node/chain-sync-state :subs/general-keys)
(reg-root-key-sub :desktop/desktop :desktop/desktop :subs/general-keys)
(reg-root-key-sub :desktop :desktop :subs/general-keys)
(reg-root-key-sub :animations :animations :subs/general-keys)
(reg-root-key-sub :get-network :network :subs/general-keys)
(reg-root-key-sub :ui/search :ui/search :subs/general-keys)
(reg-root-key-sub :web3-node-version :web3-node-version :subs/general-keys)
(reg-root-key-sub :keyboard-height :keyboard-height :subs/general-keys)
(reg-root-key-sub :sync-data :sync-data :subs/general-keys)
(reg-root-key-sub :layout-height :layout-height :subs/general-keys)
(reg-root-key-sub :mobile-network/remember-choice? :mobile-network/remember-choice? :subs/general-keys)
(reg-root-key-sub :qr-modal :qr-modal :subs/general-keys)
(reg-root-key-sub :content-layout-height :content-layout-height :subs/general-keys)
(reg-root-key-sub :bootnodes/manage :bootnodes/manage :subs/general-keys)
(reg-root-key-sub :networks/networks :networks/networks :subs/general-keys)
(reg-root-key-sub :networks/manage :networks/manage :subs/general-keys)
(reg-root-key-sub :get-pairing-installations :pairing/installations :subs/general-keys)
(reg-root-key-sub :network/type :network/type :subs/general-keys)
(reg-root-key-sub :tooltips :tooltips :subs/general-keys)

;;profile
(reg-root-key-sub :my-profile/seed :my-profile/seed :subs/profile-keys)
(reg-root-key-sub :my-profile/advanced? :my-profile/advanced? :subs/profile-keys)
(reg-root-key-sub :my-profile/editing? :my-profile/editing? :subs/profile-keys)
(reg-root-key-sub :extensions/profile :extensions/profile :subs/profile-keys)
(reg-root-key-sub :my-profile/profile :my-profile/profile :subs/profile-keys)
;;account
(reg-root-key-sub :accounts/accounts :accounts/accounts :subs/account-keys)
(reg-root-key-sub :accounts/login :accounts/login :subs/account-keys)
(reg-root-key-sub :account/account :account/account :subs/account-keys)
(reg-root-key-sub :accounts/create :accounts/create :subs/account-keys)
(reg-root-key-sub :get-recover-account :accounts/recover :subs/account-keys)
;;chat
(reg-root-key-sub ::cooldown-enabled? :chat/cooldown-enabled? :subs/chat-keys)
(reg-root-key-sub ::chats :chats :subs/chat-keys)
(reg-root-key-sub ::access-scope->command-id :access-scope->command-id :subs/chat-keys)
(reg-root-key-sub ::chat-ui-props :chat-ui-props :subs/chat-keys)
(reg-root-key-sub :chats/id->command :id->command :subs/chat-keys)
(reg-root-key-sub :chats/current-chat-id :current-chat-id :subs/chat-keys)
(reg-root-key-sub :public-group-topic :public-group-topic :subs/chat-keys)
(reg-root-key-sub :chats/loading? :chats/loading? :subs/chat-keys)
(reg-root-key-sub :new-chat-name :new-chat-name :subs/chat-keys)
(reg-root-key-sub :group-chat-profile/editing? :group-chat-profile/editing? :subs/chat-keys)
(reg-root-key-sub :group-chat-profile/profile :group-chat-profile/profile :subs/chat-keys)
(reg-root-key-sub :selected-participants :selected-participants :subs/chat-keys)

;;browser
(reg-root-key-sub :browsers :browser/browsers :subs/browser-keys)
(reg-root-key-sub :browser/options :browser/options :subs/browser-keys)
(reg-root-key-sub :dapps/permissions :dapps/permissions :subs/browser-keys)

;;stickers
(reg-root-key-sub :stickers/selected-pack :stickers/selected-pack :subs/stickers-keys)
(reg-root-key-sub :stickers/packs :stickers/packs :subs/stickers-keys)
(reg-root-key-sub :stickers/installed-packs :stickers/packs-installed :subs/stickers-keys)
(reg-root-key-sub :stickers/packs-owned :stickers/packs-owned :subs/stickers-keys)
(reg-root-key-sub :stickers/packs-pendning :stickers/packs-pendning :subs/stickers-keys)

;;mailserver
(reg-root-key-sub :mailserver/current-id :mailserver/current-id :subs/mailserver-keys)
(reg-root-key-sub :mailserver/mailservers :mailserver/mailservers :subs/mailserver-keys)
(reg-root-key-sub :mailserver.edit/mailserver :mailserver.edit/mailserver :subs/mailserver-keys)
(reg-root-key-sub :mailserver/state :mailserver/state :subs/mailserver-keys)
(reg-root-key-sub :mailserver/pending-requests :mailserver/pending-requests :subs/mailserver-keys)
(reg-root-key-sub :mailserver/request-error? :mailserver/request-error :subs/mailserver-keys)
(reg-root-key-sub :mailserver/fetching-gaps-in-progress :mailserver/fetching-gaps-in-progress :subs/mailserver-keys)
(reg-root-key-sub :mailserver/gaps :mailserver/gaps :subs/mailserver-keys)
(reg-root-key-sub :mailserver/ranges :mailserver/ranges :subs/mailserver-keys)

;;contacts
(reg-root-key-sub ::contacts :contacts/contacts :subs/contacts-keys)
(reg-root-key-sub :contacts/current-contact-identity :contacts/identity :subs/contacts-keys)
(reg-root-key-sub :new-identity-error :contacts/new-identity-error :subs/general-keys)
(reg-root-key-sub :contacts/new-identity :contacts/new-identity :subs/general-keys)
(reg-root-key-sub :group/selected-contacts :group/selected-contacts :subs/general-keys)
;;wallet
(reg-root-key-sub :wallet :wallet :subs/wallet-keys)
(reg-root-key-sub :prices :prices :subs/wallet-keys)
(reg-root-key-sub :collectibles :collectibles :subs/wallet-keys)
(reg-root-key-sub :wallet/all-tokens :wallet/all-tokens :subs/wallet-keys)
(reg-root-key-sub :prices-loading? :prices-loading? :subs/wallet-keys)
(reg-root-key-sub :wallet.transactions :wallet.transactions :subs/wallet-keys)

(doseq [[subs-key db-keys] @subs-keys]
  (reg-sub
   subs-key
   (fn [db]
     (select-keys db db-keys))))

;;GENERAL ==============================================================================================================

(reg-sub
 :connection-stats
 :<- [:desktop/desktop]
 (fn [desktop _]
   (get desktop :debug-metrics)))

(reg-sub
 :settings/logging-enabled
 :<- [:desktop/desktop]
 (fn [desktop _]
   (get desktop :logging-enabled false)))

;;TODO we have network in two different places see :account/network, what's the difference?
(reg-sub
 :network
 :<- [:account/account]
 (fn [current-account]
   (get (:networks current-account) (:network current-account))))

(reg-sub
 :disconnected?
 :<- [:peers-count]
 (fn [peers-count]
   (zero? peers-count)))

(reg-sub
 :offline?
 :<- [:network-status]
 :<- [:sync-state]
 :<- [:disconnected?]
 (fn [[network-status sync-state disconnected?]]
   (or disconnected?
       (= network-status :offline)
       (= sync-state :offline))))

(reg-sub
 :syncing?
 :<- [:sync-state]
 (fn [sync-state]
   (#{:pending :in-progress} sync-state)))

(reg-sub
 :dimensions/window-width
 :<- [:dimensions/window]
 :width)

(reg-sub
 :get-screen-params
 :<- [:screen-params]
 :<- [:view-id]
 (fn [[params view-id-db] [_ view-id]]
   (get params (or view-id view-id-db))))

(reg-sub
 :can-navigate-back?
 :<- [:navigation-stack]
 (fn [stack]
   (> (count stack) 1)))

(reg-sub
 :delete-swipe-position
 :<- [:animations]
 (fn [animations [_ type item-id]]
   (get-in animations [type item-id :delete-swiped])))

(reg-sub
 :search/filter
 :<- [:ui/search]
 (fn [search]
   (get search :filter)))

(defn- node-version [web3-node-version]
  (str "status-go v" (or web3-node-version "N/A") ""))

(def app-short-version
  (let [version (if platform/desktop? build/version build/build-no)]
    (str build/version " (" version ")")))

(reg-sub
 :get-app-version
 :<- [:web3-node-version]
 (fn [web3-node-version]
   (str app-short-version "; " (node-version web3-node-version))))

(reg-sub
 :get-app-short-version
 (fn [db] app-short-version))

(reg-sub
 :get-app-node-version
 :<- [:web3-node-version]
 node-version)

(reg-sub
 :my-profile/recovery
 :<- [:my-profile/seed]
 (fn [seed]
   (or seed {:step :intro})))

(reg-sub
 :bottom-sheet
 :<- [:bottom-sheet/show?]
 :<- [:bottom-sheet/view]
 (fn [[show? view]]
   {:show? show?
    :view  view}))

(reg-sub
 :is-contact-selected?
 :<- [:group/selected-contacts]
 (fn [selected-contacts [_ element]]
   (-> selected-contacts
       (contains? element))))

(reg-sub
 :is-participant-selected?
 :<- [:selected-participants]
 (fn [selected-participants [_ element]]
   (-> selected-participants
       (contains? element))))

;;ACCOUNT ==============================================================================================================

(reg-sub
 :account/public-key
 :<- [:account/account]
 (fn [{:keys [public-key]}]
   public-key))

(reg-sub
 :account/hex-address
 :<- [:account/account]
 (fn [{:keys [address]}]
   (ethereum/normalized-address address)))

(reg-sub
 :sign-in-enabled?
 :<- [:accounts/login]
 :<- [:node-status]
 (fn [[{:keys [password]} status]]
   (and (or (nil? status) (= status :stopped))
        (spec/valid? ::accounts.db/password
                     (security/safe-unmask-data password)))))

(reg-sub
 :settings/current-fleet
 :<- [:account-settings]
 (fn [sett]
   (fleet/current-fleet-sub sett)))

(reg-sub
 :get-account-creation-next-enabled?
 :<- [:accounts/create]
 (fn [create]
   (accounts.db/account-creation-next-enabled? create)))

(reg-sub
 :account-settings
 :<- [:account/account]
 (fn [acc]
   (get acc :settings)))

;;TODO we have network in two different places see :network, what's the difference?
(reg-sub
 :account/network
 :<- [:account/account]
 :<- [:get-network]
 (fn [[account network]]
   (get-in account [:networks network])))

(reg-sub
 :current-network-initialized?
 :<- [:account/network]
 (fn [network]
   (boolean network)))

(reg-sub
 :current-network-uses-rpc?
 :<- [:account/network]
 (fn [network]
   (get-in network [:config :UpstreamConfig :Enabled])))

(reg-sub
 :latest-block-number
 (fn [{:node/keys [latest-block-number]} _]
   (if latest-block-number latest-block-number 0)))

(reg-sub
 :settings/current-log-level
 :<- [:account-settings]
 (fn [sett]
   (or (get sett :log-level)
       config/log-level-status-go)))

;;CHAT ==============================================================================================================

(reg-sub
 :get-collectible-token
 :<- [:collectibles]
 (fn [collectibles [_ {:keys [symbol token]}]]
   (get-in collectibles [(keyword symbol) (js/parseInt token)])))

(reg-sub
 ::show-suggestions-view?
 :<- [:chats/current-chat-ui-prop :show-suggestions?]
 :<- [:chats/current-chat]
 :<- [:chats/all-available-commands]
 (fn [[show-suggestions? {:keys [input-text]} commands]]
   (and (or show-suggestions?
            (commands.input/starts-as-command? (string/trim (or input-text ""))))
        (seq commands))))

(reg-sub
 ::show-suggestions?
 :<- [::show-suggestions-view?]
 :<- [:chats/selected-chat-command]
 (fn [[show-suggestions-box? selected-command]]
   (and show-suggestions-box? (not selected-command))))

(reg-sub
 ::get-commands-for-chat
 :<- [:chats/id->command]
 :<- [::access-scope->command-id]
 :<- [:chats/current-chat]
 (fn [[id->command access-scope->command-id chat]]
   (commands/chat-commands id->command access-scope->command-id chat)))

(reg-sub
 :chats/chat
 :<- [:chats/active-chats]
 (fn [chats [_ chat-id]]
   (get chats chat-id)))

(reg-sub
 :chats/content-layout-height
 :<- [:content-layout-height]
 :<- [:chats/current-chat-ui-prop :input-height]
 :<- [:chats/current-chat-ui-prop :input-focused?]
 :<- [:keyboard-height]
 :<- [:chats/current-chat-ui-prop :show-stickers?]
 (fn [[home-content-layout-height input-height input-focused? kheight stickers?]]
   (- (+ home-content-layout-height tabs.styles/tabs-height)
      (if platform/iphone-x?
        (* 2 toolbar.styles/toolbar-height)
        toolbar.styles/toolbar-height)
      (if input-height input-height 0)
      (if stickers?
        (stickers.styles/stickers-panel-height)
        kheight)
      (if input-focused?
        (cond
          platform/iphone-x? 0
          platform/ios? tabs.styles/tabs-diff
          :else 0)
        (cond
          platform/iphone-x? (* 2 tabs.styles/minimized-tabs-height)
          platform/ios? tabs.styles/tabs-height
          :else tabs.styles/minimized-tabs-height)))))

(reg-sub
 :chats/current-chat-ui-props
 :<- [::chat-ui-props]
 :<- [:chats/current-chat-id]
 (fn [[chat-ui-props id]]
   (get chat-ui-props id)))

(reg-sub
 :chats/current-chat-ui-prop
 :<- [:chats/current-chat-ui-props]
 (fn [ui-props [_ prop]]
   (get ui-props prop)))

(reg-sub
 :chats/validation-messages
 :<- [:chats/current-chat-ui-props]
 (fn [ui-props]
   (some-> ui-props :validation-messages)))

(reg-sub
 :chats/input-margin
 :<- [:keyboard-height]
 (fn [kb-height]
   (cond
     (and platform/iphone-x? (> kb-height 0))
     (- kb-height (* 2 tabs.styles/minimized-tabs-height))

     platform/ios?
     (+ kb-height (- (if (> kb-height 0)
                       tabs.styles/minimized-tabs-height
                       0)))

     :default 0)))

(reg-sub
 :chats/active-chats
 :<- [:contacts/contacts]
 :<- [::chats]
 :<- [:account/account]
 (fn [[contacts chats account]]
   (chat.db/active-chats contacts chats account)))

(reg-sub
 :chats/current-chat
 :<- [:chats/active-chats]
 :<- [:chats/current-chat-id]
 (fn [[chats current-chat-id]]
   (let [current-chat (get chats current-chat-id)
         messages     (:messages current-chat)]
     (if (empty? messages)
       (assoc current-chat :universal-link (links/generate-link :public-chat :external current-chat-id))
       current-chat))))

(reg-sub
 :chats/current-chat-message
 :<- [:chats/current-chat]
 (fn [{:keys [messages]} [_ message-id]]
   (get messages message-id)))

(reg-sub
 :chats/current-chat-messages
 :<- [:chats/current-chat]
 (fn [{:keys [messages]}]
   (or messages {})))

(reg-sub
 :chats/current-chat-message-groups
 :<- [:chats/current-chat]
 (fn [{:keys [message-groups]}]
   (or message-groups {})))

(reg-sub
 :chats/current-chat-message-statuses
 :<- [:chats/current-chat]
 (fn [{:keys [message-statuses]}]
   (or message-statuses {})))

(reg-sub
 :chats/current-chat-referenced-messages
 :<- [:chats/current-chat]
 (fn [{:keys [referenced-messages]}]
   (or referenced-messages {})))

(re-frame/reg-sub
 :chats/messages-gaps
 :<- [:mailserver/gaps]
 :<- [:chats/current-chat-id]
 (fn [[gaps chat-id]]
   (sort-by :from (vals (get gaps chat-id)))))

(re-frame/reg-sub
 :chats/range
 :<- [:mailserver/ranges]
 :<- [:chats/current-chat-id]
 (fn [[ranges chat-id]]
   (get ranges chat-id)))

(re-frame/reg-sub
 :chats/all-loaded?
 :<- [:chats/current-chat]
 (fn [chat]
   (:all-loaded? chat)))

(re-frame/reg-sub
 :chats/public?
 :<- [:chats/current-chat]
 (fn [chat]
   (:public? chat)))

(re-frame/reg-sub
 :chats/current-chat-messages-stream
 :<- [:chats/current-chat-messages]
 :<- [:chats/current-chat-message-groups]
 :<- [:chats/current-chat-message-statuses]
 :<- [:chats/current-chat-referenced-messages]
 :<- [:chats/messages-gaps]
 :<- [:chats/range]
 :<- [:chats/all-loaded?]
 :<- [:chats/public?]
 (fn [[messages message-groups message-statuses referenced-messages
       messages-gaps range all-loaded? public?]]
   (-> (chat.db/sort-message-groups message-groups messages)
       (chat.db/messages-with-datemarks-and-statuses
        messages message-statuses referenced-messages
        messages-gaps range all-loaded? public?)
       chat.db/messages-stream)))

(reg-sub
 :chats/current-chat-intro-status
 :<- [:chats/current-chat]
 :<- [:chats/current-chat-messages]
 (fn [[{:keys [might-have-join-time-messages?]} messages]]
   (if might-have-join-time-messages?
     :loading
     (if (empty? messages)
       :empty
       :messages))))

(reg-sub
 :chats/available-commands
 :<- [::get-commands-for-chat]
 :<- [:chats/current-chat]
 (fn [[commands chat]]
   (chat.db/available-commands commands chat)))

(reg-sub
 :chats/all-available-commands
 :<- [::get-commands-for-chat]
 (fn [commands]
   (chat.db/map->sorted-seq commands)))

(reg-sub
 :chats/selected-chat-command
 :<- [:chats/current-chat]
 :<- [:chats/current-chat-ui-prop :selection]
 :<- [::get-commands-for-chat]
 (fn [[{:keys [input-text]} selection commands]]
   (commands.input/selected-chat-command input-text selection commands)))

(reg-sub
 :chats/input-placeholder
 :<- [:chats/current-chat]
 :<- [:chats/selected-chat-command]
 (fn [[{:keys [input-text]} {:keys [params current-param-position cursor-in-the-end?]}]]
   (when (and cursor-in-the-end? (string/ends-with? (or input-text "") chat.constants/spacing-char))
     (get-in params [current-param-position :placeholder]))))

(reg-sub
 :chats/parameter-box
 :<- [:chats/current-chat]
 :<- [:chats/selected-chat-command]
 (fn [[_ {:keys [current-param-position params]}]]
   (when (and params current-param-position)
     (get-in params [current-param-position :suggestions]))))

(reg-sub
 :chats/show-parameter-box?
 :<- [:chats/parameter-box]
 :<- [::show-suggestions?]
 :<- [:chats/validation-messages]
 :<- [:chats/selected-chat-command]
 (fn [[chat-parameter-box show-suggestions? validation-messages {:keys [command-completion]}]]
   (and chat-parameter-box
        (not validation-messages)
        (not show-suggestions?)
        (not (= :complete command-completion)))))

(reg-sub
 :chats/unviewed-messages-count
 (fn [[_ chat-id]]
   (subscribe [:chats/chat chat-id]))
 (fn [{:keys [unviewed-messages-count]}]
   unviewed-messages-count))

(reg-sub
 :chats/photo-path
 :<- [:contacts/contacts]
 :<- [:account/account]
 (fn [[contacts account] [_ id]]
   (or (:photo-path (contacts id))
       (when (= id (:public-key account))
         (:photo-path account)))))

(reg-sub
 :chats/unread-messages-number
 :<- [:chats/active-chats]
 (fn [chats _]
   (apply + (map :unviewed-messages-count (vals chats)))))

(reg-sub
 :chats/cooldown-enabled?
 :<- [:chats/current-chat]
 :<- [::cooldown-enabled?]
 (fn [[{:keys [public?]} cooldown-enabled?]]
   (and public?
        cooldown-enabled?)))

(reg-sub
 :chats/reply-message
 :<- [:chats/current-chat]
 (fn [{:keys [metadata messages]}]
   (get messages (get-in metadata [:responding-to-message :message-id]))))

(reg-sub
 :public-chat.new/topic-error-message
 :<- [:public-group-topic]
 (fn [topic]
   (when-not (or (empty? topic)
                 (db/valid-topic? topic))
     (i18n/label :topic-name-error))))

(defn filter-selected-contacts
  [selected-contacts contacts]
  (filter #(contact.db/added? (contacts %)) selected-contacts))

(reg-sub
 :selected-contacts-count
 :<- [:group/selected-contacts]
 :<- [:contacts/contacts]
 (fn [[selected-contacts contacts]]
   (count (filter-selected-contacts selected-contacts contacts))))

(reg-sub
 :selected-participants-count
 :<- [:selected-participants]
 (fn [selected-participants]
   (count selected-participants)))

(defn filter-contacts [selected-contacts active-contacts]
  (filter #(selected-contacts (:public-key %)) active-contacts))

(reg-sub
 :selected-group-contacts
 :<- [:group/selected-contacts]
 :<- [:contacts/active]
 (fn [[selected-contacts active-contacts]]
   (filter-contacts selected-contacts active-contacts)))

(reg-sub
 :chats/transaction-confirmed?
 :<- [:wallet-transactions]
 (fn [txs [_ tx-hash]]
   (-> (get-in txs [tx-hash :confirmations] "0")
       (js/parseInt)
       (>= transactions/confirmations-count-threshold))))

(reg-sub
 :chats/wallet-transaction-exists?
 :<- [:wallet-transactions]
 (fn [txs [_ tx-hash]]
   (not (nil? (get txs tx-hash)))))

;;BOOTNODES ============================================================================================================

(reg-sub
 :settings/bootnodes-enabled
 :<- [:account/account]
 (fn [account]
   (let [{:keys [network settings]} account]
     (get-in settings [:bootnodes network]))))

(reg-sub
 :settings/network-bootnodes
 :<- [:account/account]
 (fn [account]
   (get-in account [:bootnodes (:network account)])))

(reg-sub
 :get-manage-bootnode
 :<- [:bootnodes/manage]
 (fn [manage]
   manage))

(reg-sub
 :manage-bootnode-validation-errors
 :<- [:get-manage-bootnode]
 (fn [manage]
   (set (keep
         (fn [[k {:keys [error]}]]
           (when error k))
         manage))))

;;BROWSER ==============================================================================================================

(reg-sub
 :browser/browsers
 :<- [:browsers]
 (fn [browsers]
   (reduce (fn [acc [k browser]]
             (update acc k assoc :url (browser/get-current-url browser)))
           browsers
           browsers)))

(reg-sub
 :browser/browsers-vals
 :<- [:browser/browsers]
 (fn [browsers]
   (sort-by :timestamp > (vals browsers))))

(reg-sub
 :get-current-browser
 :<- [:browser/options]
 :<- [:browser/browsers]
 (fn [[options browsers]]
   (let [browser (get browsers (:browser-id options))]
     (assoc browser :secure? (browser/secure? browser options)))))

;;STICKERS =============================================================================================================

(reg-sub
 :stickers/installed-packs-vals
 :<- [:stickers/installed-packs]
 (fn [packs]
   (vals packs)))

(reg-sub
 :stickers/all-packs
 :<- [:stickers/packs]
 :<- [:stickers/installed-packs]
 :<- [:stickers/packs-owned]
 :<- [:stickers/packs-pendning]
 (fn [[packs installed owned pending]]
   (map (fn [{:keys [id] :as pack}]
          (cond-> pack
            (get installed id) (assoc :installed true)
            (get owned id) (assoc :owned true)
            (get pending id) (assoc :pending true)))
        (vals packs))))

(reg-sub
 :stickers/get-current-pack
 :<- [:get-screen-params]
 :<- [:stickers/all-packs]
 (fn [[{:keys [id]} packs]]
   (first (filter #(= (:id %) id) packs))))

(defn find-pack-id-for-uri [sticker-uri packs]
  (some (fn [{:keys [stickers id]}]
          (when (some #(= sticker-uri (:uri %)) stickers)
            id))
        packs))

(reg-sub
 :stickers/recent
 :<- [:account/account]
 :<- [:stickers/installed-packs-vals]
 (fn [[{:keys [recent-stickers]} packs]]
   (map (fn [uri] {:uri uri :pack (find-pack-id-for-uri uri packs)}) recent-stickers)))

;;EXTENSIONS ===========================================================================================================

(reg-sub
 :extensions/all-extensions
 :<- [:account/account]
 (fn [account]
   (get account :extensions)))

;;HOME =================================================================================================================

(reg-sub
 :home-items
 :<- [:chats/active-chats]
 :<- [:search/filter]
 :<- [:search/filtered-chats]
 (fn [[chats search-filter filtered-chats]]
   (if (or (nil? search-filter)
           (and platform/desktop? (empty? search-filter)))
     {:all-home-items
      (sort-by #(-> % second :timestamp) > chats)}
     {:search-filter search-filter
      :chats         filtered-chats})))

;;NETWORK SETTINGS =====================================================================================================

(reg-sub
 :get-network-id
 :<- [:network]
 (fn [network]
   (ethereum/network->chain-id network)))

(defn- filter-networks [network-type]
  (fn [network]
    (let [chain-id (ethereum/network->chain-id network)
          testnet? (ethereum/testnet? chain-id)
          custom?  (:custom? network)]
      (case network-type
        :custom custom?
        :mainnet (and (not custom?) (not testnet?))
        :testnet (and (not custom?) testnet?)))))

(defn- label-networks [default-networks]
  (fn [network]
    (let [custom? (not (contains? default-networks (:id network)))]
      (assoc network :custom? custom?))))

(reg-sub
 :get-networks
 :<- [:account/account]
 :<- [:networks/networks]
 (fn [[{:keys [networks] :as account} default-networks]]
   (let [networks (map (label-networks default-networks) (sort-by :name (vals networks)))
         types    [:mainnet :testnet :custom]]
     (zipmap
      types
      (map #(filter (filter-networks %) networks) types)))))

(reg-sub
 :get-manage-network
 :<- [:networks/manage]
 (fn [manage]
   manage))

(reg-sub
 :manage-network-valid?
 :<- [:get-manage-network]
 (fn [manage]
   (not-any? :error (vals manage))))

;;PAIRING ==============================================================================================================

(reg-sub
 :pairing/installations
 :<- [:get-pairing-installations]
 (fn [installations]
   (->> installations
        vals
        (sort-by (comp unchecked-negate :last-paired)))))

(reg-sub
 :pairing/installation-id
 :<- [:account/account]
 :installation-id)

(reg-sub
 :pairing/installation-name
 :<- [:account/account]
 (fn [account] (:installation-name account)))

;;PROFILE ==============================================================================================================

(reg-sub
 :get-profile-unread-messages-number
 :<- [:account/account]
 (fn [{:keys [seed-backed-up? mnemonic]}]
   (if (or seed-backed-up? (string/blank? mnemonic)) 0 1)))

;;WALLET ==============================================================================================================

(reg-sub
 :balance
 :<- [:wallet]
 (fn [wallet]
   (:balance wallet)))

(reg-sub
 :price
 :<- [:prices]
 (fn [prices [_ fsym tsym]]
   (get-in prices [fsym tsym :price])))

(reg-sub
 :last-day
 :<- [:prices]
 (fn [prices [_ fsym tsym]]
   (get-in prices [fsym tsym :last-day])))

(reg-sub
 :wallet-transactions
 :<- [:wallet]
 (fn [wallet]
   (get wallet :transactions)))

(reg-sub
 :wallet.settings/currency
 :<- [:account-settings]
 (fn [sett]
   (or (get-in sett [:wallet :currency]) :usd)))

(reg-sub
 :wallet.transactions/filters
 :<- [:wallet.transactions]
 (fn [txs]
   (get txs :filters)))

(reg-sub
 :asset-value
 (fn [[_ fsym decimals tsym]]
   [(subscribe [:balance])
    (subscribe [:price fsym tsym])
    (subscribe [:wallet/currency])])
 (fn [[balance price currency] [_ fsym decimals tsym]]
   (when (and balance price)
     (-> (money/internal->formatted (get balance fsym) fsym decimals)
         (money/crypto->fiat price)
         (money/with-precision 2)
         str
         (i18n/format-currency (:code currency))))))

(defn- get-balance-total-value [balance prices currency token->decimals]
  (reduce-kv (fn [acc symbol value]
               (if-let [price (get-in prices [symbol currency :price])]
                 (+ acc (-> (money/internal->formatted value symbol (token->decimals symbol))
                            (money/crypto->fiat price)
                            .toNumber))
                 acc)) 0 balance))

(reg-sub
 :portfolio-value
 :<- [:balance]
 :<- [:prices]
 :<- [:wallet/currency]
 :<- [:network]
 :<- [:wallet/all-tokens]
 (fn [[balance prices currency network all-tokens] [_ currency-code]]
   (if (and balance prices)
     (let [assets          (tokens/tokens-for all-tokens (ethereum/network->chain-keyword network))
           token->decimals (into {} (map #(vector (:symbol %) (:decimals %)) assets))
           balance-total-value
           (get-balance-total-value balance
                                    prices
                                    (or currency-code
                                        (-> currency :code keyword))
                                    token->decimals)]
       (if (pos? balance-total-value)
         (-> balance-total-value
             (money/with-precision 2)
             str
             (i18n/format-currency (:code currency) false))
         "0"))
     "...")))

(reg-sub
 :wallet/balance-loading?
 :<- [:wallet]
 (fn [wallet]
   (:balance-loading? wallet)))

(reg-sub
 :wallet/error-message
 :<- [:wallet]
 (fn [wallet]
   (or (get-in wallet [:errors :balance-update])
       (get-in wallet [:errors :prices-update]))))

(reg-sub
 :get-wallet-unread-messages-number
 (fn [db]
   0))

(reg-sub
 :wallet/visible-tokens-symbols
 :<- [:network]
 :<- [:account/account]
 (fn [[network current-account]]
   (let [chain (ethereum/network->chain-keyword network)]
     (get-in current-account [:settings :wallet :visible-tokens chain]))))

(reg-sub
 :wallet/visible-assets
 :<- [:network]
 :<- [:wallet/visible-tokens-symbols]
 :<- [:wallet/all-tokens]
 (fn [[network visible-tokens-symbols all-tokens]]
   (let [chain (ethereum/network->chain-keyword network)]
     (conj (filter #(contains? visible-tokens-symbols (:symbol %))
                   (tokens/sorted-tokens-for all-tokens (ethereum/network->chain-keyword network)))
           (tokens/native-currency chain)))))

(reg-sub
 :wallet/visible-assets-with-amount
 :<- [:balance]
 :<- [:wallet/visible-assets]
 (fn [[balance visible-assets]]
   (map #(assoc % :amount (get balance (:symbol %))) visible-assets)))

(reg-sub
 :wallet/transferrable-assets-with-amount
 :<- [:wallet/visible-assets-with-amount]
 (fn [all-assets]
   (filter #(not (:nft? %)) all-assets)))

(reg-sub
 :wallet/currency
 :<- [:wallet.settings/currency]
 (fn [currency-id]
   (get constants/currencies currency-id)))

;;WALLET TRANSACTIONS ==================================================================================================

(reg-sub
 :wallet.transactions/current-tab
 :<- [:wallet]
 (fn [wallet]
   (get wallet :current-tab 0)))

(defn enrich-transaction [{:keys [type to from timestamp] :as transaction} contacts]
  (let [[contact-address key-contact key-wallet] (if (= type :inbound)
                                                   [from :from-contact :to-wallet]
                                                   [to :to-contact :from-wallet])
        wallet  (i18n/label :main-wallet)
        contact (get contacts (utils.hex/normalize-hex contact-address))]
    (cond-> transaction
      contact (assoc key-contact (:name contact))
      :always (assoc key-wallet wallet
                     :time-formatted (datetime/timestamp->time timestamp)))))

(reg-sub
 :wallet.transactions/transactions
 :<- [:wallet]
 :<- [:contacts/contacts-by-address]
 (fn [[wallet contacts]]
   (reduce (fn [acc [hash transaction]]
             (assoc acc hash (enrich-transaction transaction contacts)))
           {}
           (:transactions wallet))))

(reg-sub
 :wallet.transactions/grouped-transactions
 :<- [:wallet.transactions/transactions]
 (fn [transactions]
   (group-by :type (vals transactions))))

(reg-sub
 :wallet.transactions/pending-transactions-list
 :<- [:wallet.transactions/grouped-transactions]
 (fn [{:keys [pending]}]
   (when pending
     {:title "Pending"
      :key   :pending
      :data  pending})))

(reg-sub
 :wallet.transactions/failed-transactions-list
 :<- [:wallet.transactions/grouped-transactions]
 (fn [{:keys [failed]}]
   (when failed
     {:title "Failed"
      :key   :failed
      :data  failed})))

(defn group-transactions-by-date [transactions]
  (->> transactions
       (group-by #(datetime/timestamp->date-key (:timestamp %)))
       (sort-by key)
       reverse
       (map (fn [[date-key transactions]]
              {:title (datetime/timestamp->mini-date (:timestamp (first transactions)))
               :key   date-key
               :data  (sort-by :timestamp > transactions)}))))

(reg-sub
 :wallet.transactions/completed-transactions-list
 :<- [:wallet.transactions/grouped-transactions]
 (fn [{:keys [inbound outbound failed]}]
   (group-transactions-by-date (concat inbound outbound failed))))

(reg-sub
 :wallet.transactions/transactions-history-list
 :<- [:wallet.transactions/pending-transactions-list]
 :<- [:wallet.transactions/completed-transactions-list]
 (fn [[pending completed]]
   (cond-> []
     pending (into pending)
     completed (into completed))))

(reg-sub
 :wallet.transactions/current-transaction
 :<- [:wallet]
 (fn [wallet]
   (:current-transaction wallet)))

(reg-sub
 :wallet.transactions/transaction-details
 :<- [:wallet.transactions/transactions]
 :<- [:wallet.transactions/current-transaction]
 :<- [:network]
 (fn [[transactions current-transaction network]]
   (let [{:keys [gas-used gas-price hash timestamp type] :as transaction} (get transactions current-transaction)
         chain           (ethereum/network->chain-keyword network)
         native-currency (tokens/native-currency chain)
         display-unit    (wallet.utils/display-symbol native-currency)]
     (when transaction
       (merge transaction
              {:gas-price-eth  (if gas-price (money/wei->str :eth gas-price display-unit) "-")
               :gas-price-gwei (if gas-price (money/wei->str :gwei gas-price) "-")
               :date           (datetime/timestamp->long-date timestamp)}
              (if (= type :unsigned)
                {:block     (i18n/label :not-applicable)
                 :cost      (i18n/label :not-applicable)
                 :gas-limit (i18n/label :not-applicable)
                 :gas-used  (i18n/label :not-applicable)
                 :nonce     (i18n/label :not-applicable)
                 :hash      (i18n/label :not-applicable)}
                {:cost (when gas-used
                         (money/wei->str :eth (money/fee-value gas-used gas-price) display-unit))
                 :url  (transactions/get-transaction-details-url chain hash)}))))))

(reg-sub
 :wallet.transactions.details/confirmations
 :<- [:wallet.transactions/transaction-details]
 (fn [transaction-details]
   ;;TODO (yenda) this field should be calculated based on the current-block and the block of the transaction
   (:confirmations transaction-details)))

(reg-sub
 :wallet.transactions.details/confirmations-progress
 :<- [:wallet.transactions.details/confirmations]
 (fn [confirmations]
   (let [max-confirmations 10]
     (if (>= confirmations max-confirmations)
       100
       (* 100 (/ confirmations max-confirmations))))))

;;WALLET SEND ==========================================================================================================

(reg-sub
 ::send-transaction
 :<- [:wallet]
 (fn [wallet]
   (:send-transaction wallet)))

(reg-sub
 :wallet.send/symbol
 :<- [::send-transaction]
 (fn [send-transaction]
   (:symbol send-transaction)))

(reg-sub
 :wallet.send/advanced?
 :<- [::send-transaction]
 (fn [send-transaction]
   (:advanced? send-transaction)))

(reg-sub
 :wallet.send/camera-flashlight
 :<- [::send-transaction]
 (fn [send-transaction]
   (:camera-flashlight send-transaction)))

(reg-sub
 :wallet.send/wrong-password?
 :<- [::send-transaction]
 (fn [send-transaction]
   (:wrong-password? send-transaction)))

(reg-sub
 :wallet.send/sign-password-enabled?
 :<- [::send-transaction]
 (fn [{:keys [password]}]
   (and (not (nil? password)) (not= password ""))))

(defn edit-or-transaction-data
  "Set up edit data structure, defaulting to transaction when not available"
  [transaction edit]
  (cond-> edit
    (not (get-in edit [:gas-price :value]))
    (models.wallet/build-edit
     :gas-price
     (money/to-fixed (money/wei-> :gwei (:gas-price transaction))))

    (not (get-in edit [:gas :value]))
    (models.wallet/build-edit
     :gas
     (money/to-fixed (:gas transaction)))))

(reg-sub
 :wallet/edit
 :<- [::send-transaction]
 :<- [:wallet]
 (fn [[send-transaction {:keys [edit]}]]
   (edit-or-transaction-data
    send-transaction
    edit)))

(defn check-sufficient-funds [transaction balance symbol amount]
  (assoc transaction :sufficient-funds?
         (or (nil? amount)
             (money/sufficient-funds? amount (get balance symbol)))))

(defn check-sufficient-gas [transaction balance symbol amount]
  (assoc transaction :sufficient-gas?
         (or (nil? amount)
             (let [available-ether   (get balance :ETH (money/bignumber 0))
                   available-for-gas (if (= :ETH symbol)
                                       (.minus available-ether (money/bignumber amount))
                                       available-ether)]
               (money/sufficient-funds? (-> transaction
                                            :max-fee
                                            money/bignumber
                                            (money/formatted->internal :ETH 18))
                                        (money/bignumber available-for-gas))))))

(reg-sub
 :wallet.send/transaction
 :<- [::send-transaction]
 :<- [:balance]
 (fn [[{:keys [amount symbol] :as transaction} balance]]
   (-> transaction
       (models.wallet/add-max-fee)
       (check-sufficient-funds balance symbol amount)
       (check-sufficient-gas balance symbol amount))))

(reg-sub
 :wallet.send/signing-phrase-with-padding
 :<- [:account/account]
 (fn [{:keys [signing-phrase]}]
   (when signing-phrase
     (clojure.string/replace-all signing-phrase #" " "     "))))

(reg-sub
 :wallet/settings
 :<- [:wallet]
 (fn [{:keys [settings]}]
   (reduce-kv #(conj %1 %3) [] settings)))

(reg-sub
 :wallet.request/transaction
 :<- [:wallet]
 :request-transaction)

(reg-sub
 :screen-collectibles
 :<- [:collectibles]
 :<- [:get-screen-params]
 (fn [[collectibles {:keys [symbol]}]]
   (when-let [v (get collectibles symbol)]
     (mapv #(assoc (second %) :id (first %)) v))))

;;UI ==============================================================================================================

;;TODO this subscription looks super weird huge and with dispatches?
(reg-sub
 :connectivity/status-properties
 :<- [:offline?]
 :<- [:disconnected?]
 :<- [:mailserver/connecting?]
 :<- [:mailserver/connection-error?]
 :<- [:mailserver/request-error?]
 :<- [:mailserver/fetching?]
 :<- [:network/type]
 :<- [:account/account]
 (fn [[offline? disconnected? mailserver-connecting? mailserver-connection-error?
       mailserver-request-error? mailserver-fetching? network-type account]]
   (let [wallet-offline? (and offline?
                              ;; There's no wallet of desktop
                              (not platform/desktop?))
         error-label     (cond
                           (and wallet-offline?
                                disconnected?)
                           :t/offline

                           wallet-offline?
                           :t/wallet-offline

                           disconnected?
                           :t/disconnected

                           mailserver-connecting?
                           :t/connecting

                           mailserver-connection-error?
                           :t/mailserver-reconnect

                           mailserver-request-error?
                           :t/mailserver-request-error-status

                           (and (mobile-network-utils/cellular? network-type)
                                (not (:syncing-on-mobile-network? account)))
                           :mobile-network

                           :else nil)]
     {:message            (or error-label :t/connected)
      :connected?         (and (nil? error-label) (not= :mobile-network error-label))
      :connecting?        (= error-label :t/connecting)
      :loading-indicator? mailserver-fetching?
      :on-press-fn        #(cond
                             mailserver-connection-error?
                             (re-frame/dispatch [:mailserver.ui/reconnect-mailserver-pressed])
                             mailserver-request-error?
                             (re-frame/dispatch [:mailserver.ui/request-error-pressed])

                             (= :mobile-network error-label)
                             (re-frame/dispatch [:mobile-network/show-offline-sheet]))})))

;;CONTACT ==============================================================================================================

(reg-sub
 ::query-current-chat-contacts
 :<- [:chats/current-chat]
 :<- [:contacts/contacts]
 (fn [[chat contacts] [_ query-fn]]
   (contact.db/query-chat-contacts chat contacts query-fn)))

(reg-sub
 :contacts/contacts
 :<- [::contacts]
 (fn [contacts]
   (contact.db/enrich-contacts contacts)))

(reg-sub
 :contacts/active
 :<- [:contacts/contacts]
 (fn [contacts]
   (contact.db/get-active-contacts contacts)))

(reg-sub
 :contacts/active-count
 :<- [:contacts/active]
 (fn [active-contacts]
   (count active-contacts)))

(reg-sub
 :contacts/blocked
 :<- [:contacts/contacts]
 (fn [contacts]
   (->> contacts
        (filter (fn [[_ contact]]
                  (contact.db/blocked? contact)))
        (contact.db/sort-contacts))))

(reg-sub
 :contacts/blocked-count
 :<- [:contacts/blocked]
 (fn [blocked-contacts]
   (count blocked-contacts)))

(reg-sub
 :contacts/current-contact
 :<- [:contacts/contacts]
 :<- [:contacts/current-contact-identity]
 (fn [[contacts identity]]
   (or (contacts identity)
       (-> identity
           contact.db/public-key->new-contact
           contact.db/enrich-contact))))

(reg-sub
 :contacts/contact-name-by-identity
 :<- [:contacts/contacts]
 :<- [:account/account]
 (fn [[contacts current-account] [_ identity]]
   (let [me? (= (:public-key current-account) identity)]
     (if me?
       (:name current-account)
       (:name (contacts identity))))))

(reg-sub
 :contacts/all-contacts-not-in-current-chat
 :<- [::query-current-chat-contacts remove]
 (fn [contacts]
   (sort-by (comp clojure.string/lower-case :name) contacts)))

(reg-sub
 :contacts/current-chat-contacts
 :<- [:chats/current-chat]
 :<- [:contacts/contacts]
 :<- [:account/account]
 (fn [[{:keys [contacts admins]} all-contacts current-account]]
   (contact.db/get-all-contacts-in-group-chat contacts admins all-contacts current-account)))

(reg-sub
 :contacts/contacts-by-chat
 (fn [[_ _ chat-id] _]
   [(subscribe [:chats/chat chat-id])
    (subscribe [:contacts/contacts])])
 (fn [[chat all-contacts] [_ query-fn]]
   (contact.db/query-chat-contacts chat all-contacts query-fn)))

(reg-sub
 :contacts/chat-photo
 (fn [[_ chat-id] _]
   [(subscribe [:chats/chat chat-id])
    (subscribe [:contacts/contacts-by-chat filter chat-id])])
 (fn [[chat contacts] [_ chat-id]]
   (when (and chat (not (:group-chat chat)))
     (cond
       (:photo-path chat)
       (:photo-path chat)

       (pos? (count contacts))
       (:photo-path (first contacts))

       :else
       (identicon/identicon chat-id)))))

(reg-sub
 :contacts/contact-by-address
 :<- [:contacts/contacts]
 (fn [contacts [_ address]]
   (contact.db/find-contact-by-address contacts address)))

(reg-sub
 :contacts/contacts-by-address
 :<- [:contacts/contacts]
 (fn [contacts]
   (reduce (fn [acc [_ {:keys [address] :as contact}]]
             (if address
               (assoc acc address contact)
               acc))
           {}
           contacts)))

;;EXTENSIONS ============================================================================================================
;;TODO not optimized yet

(reg-sub
 :extensions/identity
 (fn [_ [_ _ {:keys [value]}]]
   value))

(defn get-token-for [network all-tokens token]
  (if (= token "ETH")
    {:decimals 18
     :address  "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"}
    (tokens/token-for (ethereum/network->chain-keyword network) all-tokens token)))

(reg-sub
 :extensions.wallet/balance
 :<- [:wallet/all-tokens]
 :<- [:network]
 :<- [:balance]
 (fn [[all-tokens network balance] [_ _ {token :token}]]
   (let [{:keys [decimals]} (get-token-for network all-tokens token)
         value (or (get balance (keyword token)) (money/bignumber 0))]
     {:value        (money/token->unit value decimals)
      :value-in-wei value})))

(reg-sub
 :extensions.wallet/token
 :<- [:wallet/all-tokens]
 :<- [:network]
 (fn [[all-tokens network] [_ _ {token :token amount :amount amount-in-wei :amount-in-wei}]]
   (let [{:keys [decimals] :as m} (get-token-for network all-tokens token)]
     (merge m
            (when amount {:amount-in-wei (money/unit->token amount decimals)})
            (when amount-in-wei {:amount (money/token->unit amount-in-wei decimals)})))))

(defn normalize-token [m]
  (update m :symbol name))

(reg-sub
 :extensions.wallet/tokens
 :<- [:wallet/all-tokens]
 :<- [:wallet/visible-tokens-symbols]
 :<- [:network]
 (fn [[all-tokens visible-tokens-symbols network] [_ _ {filter-vector :filter visible :visible}]]
   (let [tokens (map normalize-token (filter #(and (not (:nft? %)) (if visible (contains? visible-tokens-symbols (:symbol %)) true))
                                             (tokens/sorted-tokens-for all-tokens (ethereum/network->chain-keyword network))))]
     (if filter-vector
       (filter #((set filter-vector) (:symbol %)) tokens)
       tokens))))

(reg-sub
 :store/get
 (fn [db [_ {id :id} {:keys [key] :as params}]]
   (let [result (get-in db [:extensions/store id key])]
     (if (:reverse params)
       (reverse result)
       result))))

(reg-sub
 :store/get-in
 (fn [db [_ {id :id} {:keys [keys]}]]
   (get-in db (into [] (concat [:extensions/store id] keys)))))

(defn- ->contact [{:keys [photo-path address name public-key]}]
  {:photo      photo-path
   :name       name
   :address    (str "0x" address)
   :public-key public-key})

(reg-sub
 :extensions.contacts/all
 :<- [:contacts/active]
 (fn [[contacts] _]
   (map #(update % :address ->contact))))

(reg-sub
 :store/get-vals
 (fn [db [_ {id :id} {:keys [key]}]]
   (vals (get-in db [:extensions/store id key]))))

(reg-sub
 :extensions.time/now
 (fn [_ _]
   (.toLocaleString (js/Date.))))

;;MAILSERVER ===========================================================================================================

(reg-sub
 :mailserver/connecting?
 :<- [:mailserver/state]
 (fn [state]
   (#{:connecting :added} state)))

(reg-sub
 :mailserver/connection-error?
 :<- [:mailserver/state]
 (fn [state]
   (#{:error :disconnected} state)))

(reg-sub
 :chats/fetching-gap-in-progress?
 :<- [:chats/current-chat-id]
 :<- [:mailserver/fetching-gaps-in-progress]
 (fn [[chat-id gaps] [_ ids]]
   (seq (select-keys (get gaps chat-id) ids))))

(reg-sub
 :mailserver/fetching?
 :<- [:mailserver/state]
 :<- [:mailserver/pending-requests]
 :<- [:mailserver/connecting?]
 :<- [:mailserver/connection-error?]
 :<- [:mailserver/request-error?]
 (fn [[state pending-requests connecting? connection-error? request-error?]]
   (and pending-requests
        (= state :connected)
        (pos-int? pending-requests)
        (not (or connecting? connection-error? request-error?)))))

(reg-sub
 :mailserver/fleet-mailservers
 :<- [:settings/current-fleet]
 :<- [:mailserver/mailservers]
 (fn [[current-fleet mailservers]]
   (current-fleet mailservers)))

(reg-sub
 :mailserver.edit/connected?
 :<- [:mailserver.edit/mailserver]
 :<- [:mailserver/current-id]
 (fn [[mailserver current-mailserver-id]]
   (= (get-in mailserver [:id :value])
      current-mailserver-id)))

(reg-sub
 :mailserver.edit/validation-errors
 :<- [:mailserver.edit/mailserver]
 (fn [mailserver]
   (set (keep
         (fn [[k {:keys [error]}]]
           (when error k))
         mailserver))))

(reg-sub
 :mailserver/connected?
 :<- [:mailserver/state]
 :<- [:network-status]
 (fn [[mail-state network-status]]
   (let [connected? (= :connected mail-state)
         online?    (= :online network-status)]
     (and connected? online?))))

(reg-sub
 :mailserver/preferred-id
 :<- [:account-settings]
 (fn [settings]
   (get-in settings [:mailserver (fleet/current-fleet-sub settings)])))

;;SEARCH ==============================================================================================================

(defn extract-chat-attributes [chat]
  (let [{:keys [name random-name tags]} (val chat)]
    (into [name random-name] tags)))

(defn apply-filter
  "extract-attributes-fn is a function that take an element from the collection
  and returns a vector of attributes which are strings
  apply-filter returns the elements for which at least one attribute includes
  the search-filter
  apply-filter returns nil if the search-filter is empty or if there is no element
  that match the filter"
  [search-filter coll extract-attributes-fn]
  (when (not-empty search-filter)
    (let [search-filter (string/lower-case search-filter)
          results       (filter (fn [element]
                                  (some (fn [s]
                                          (when (string? s)
                                            (string/includes? (string/lower-case s)
                                                              search-filter)))
                                        (extract-attributes-fn element)))
                                coll)]
      (when (not-empty results)
        (sort-by #(-> % second :timestamp) >
                 (into {} results))))))

(reg-sub
 :search/filtered-chats
 :<- [:chats/active-chats]
 :<- [:search/filter]
 (fn [[chats search-filter]]
   (apply-filter search-filter chats extract-chat-attributes)))