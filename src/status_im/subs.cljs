(ns status-im.subs
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.browser.core :as browser]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.input :as commands.input]
            [status-im.chat.constants :as chat.constants]
            [status-im.chat.db :as chat.db]
            [status-im.chat.models :as chat.models]
            [status-im.constants :as constants]
            [status-im.contact.db :as contact.db]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.ethereum.tokens :as tokens]
            [status-im.ethereum.transactions.core :as transactions]
            [status-im.fleet.core :as fleet]
            [status-im.group-chats.db :as group-chats.db]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.multiaccounts.db :as multiaccounts.db]
            [status-im.pairing.core :as pairing]
            [status-im.tribute-to-talk.core :as tribute-to-talk]
            [status-im.tribute-to-talk.db :as tribute-to-talk.db]
            [status-im.tribute-to-talk.whitelist :as whitelist]
            [status-im.ui.components.bottom-bar.styles :as tabs.styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.toolbar.styles :as toolbar.styles]
            [status-im.ui.screens.add-new.new-public-chat.db :as db]
            [status-im.ui.screens.chat.stickers.styles :as stickers.styles]
            [status-im.ui.screens.mobile-network-settings.utils
             :as
             mobile-network-utils]
            [status-im.wallet.utils :as wallet.utils]
            [status-im.utils.build :as build]
            [status-im.utils.config :as config]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.hex :as utils.hex]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.money :as money]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]
            [status-im.utils.universal-links.core :as links]
            [status-im.wallet.core :as wallet]
            [status-im.wallet.db :as wallet.db]
            [status-im.signing.gas :as signing.gas]
            status-im.ui.screens.hardwallet.connect.subs
            status-im.ui.screens.hardwallet.settings.subs
            status-im.ui.screens.hardwallet.pin.subs
            status-im.ui.screens.hardwallet.setup.subs))

;; TOP LEVEL ===========================================================================================================

(defn reg-root-key-sub [sub-name db-key]
  (re-frame/reg-sub sub-name (fn [db] (get db db-key))))

;;view
(reg-root-key-sub :view-id :view-id)
(reg-root-key-sub :navigation-stack :navigation-stack)
(reg-root-key-sub :screen-params :navigation/screen-params)
(reg-root-key-sub :two-pane-ui-enabled? :two-pane-ui-enabled?)

;;bottom sheet
(reg-root-key-sub :bottom-sheet/show? :bottom-sheet/show?)
(reg-root-key-sub :bottom-sheet/view :bottom-sheet/view)
(reg-root-key-sub :bottom-sheet/options :bottom-sheet/options)

;;general
(reg-root-key-sub :sync-state :sync-state)
(reg-root-key-sub :network-status :network-status)
(reg-root-key-sub :peers-count :peers-count)
(reg-root-key-sub :about-app/node-info :node-info)
(reg-root-key-sub :peers-summary :peers-summary)
(reg-root-key-sub :tab-bar-visible? :tab-bar-visible?)
(reg-root-key-sub :dimensions/window :dimensions/window)
(reg-root-key-sub :initial-props :initial-props)
(reg-root-key-sub :get-device-UUID :device-UUID)
(reg-root-key-sub :fleets/custom-fleets :custom-fleets)
(reg-root-key-sub :chain-sync-state :node/chain-sync-state)
(reg-root-key-sub :desktop/desktop :desktop/desktop)
(reg-root-key-sub :desktop :desktop)
(reg-root-key-sub :animations :animations)
(reg-root-key-sub :ui/search :ui/search)
(reg-root-key-sub :web3-node-version :web3-node-version)
(reg-root-key-sub :keyboard-height :keyboard-height)
(reg-root-key-sub :sync-data :sync-data)
(reg-root-key-sub :layout-height :layout-height)
(reg-root-key-sub :mobile-network/remember-choice? :mobile-network/remember-choice?)
(reg-root-key-sub :qr-modal :qr-modal)
(reg-root-key-sub :content-layout-height :content-layout-height)
(reg-root-key-sub :bootnodes/manage :bootnodes/manage)
(reg-root-key-sub :networks/current-network :networks/current-network)
(reg-root-key-sub :networks/networks :networks/networks)
(reg-root-key-sub :networks/manage :networks/manage)
(reg-root-key-sub :get-pairing-installations :pairing/installations)
(reg-root-key-sub :tooltips :tooltips)
(reg-root-key-sub :supported-biometric-auth :supported-biometric-auth)

;;NOTE this one is not related to ethereum network
;; it is about cellular network/ wifi network
(reg-root-key-sub :network/type :network/type)

;;profile
(reg-root-key-sub :my-profile/seed :my-profile/seed)
(reg-root-key-sub :my-profile/advanced? :my-profile/advanced?)
(reg-root-key-sub :my-profile/editing? :my-profile/editing?)
(reg-root-key-sub :my-profile/profile :my-profile/profile)
;;multiaccount
(reg-root-key-sub :multiaccounts/multiaccounts :multiaccounts/multiaccounts)
(reg-root-key-sub :multiaccounts/login :multiaccounts/login)
(reg-root-key-sub :multiaccount :multiaccount)
(reg-root-key-sub :get-recover-multiaccount :multiaccounts/recover)
;;chat
(reg-root-key-sub ::cooldown-enabled? :chat/cooldown-enabled?)
(reg-root-key-sub ::chats :chats)
(reg-root-key-sub ::access-scope->command-id :access-scope->command-id)
(reg-root-key-sub ::chat-ui-props :chat-ui-props)
(reg-root-key-sub :chats/id->command :id->command)
(reg-root-key-sub :chats/current-chat-id :current-chat-id)
(reg-root-key-sub :public-group-topic :public-group-topic)
(reg-root-key-sub :chats/loading? :chats/loading?)
(reg-root-key-sub :new-chat-name :new-chat-name)
(reg-root-key-sub :group-chat-profile/editing? :group-chat-profile/editing?)
(reg-root-key-sub :group-chat-profile/profile :group-chat-profile/profile)
(reg-root-key-sub :selected-participants :selected-participants)

;;browser
(reg-root-key-sub :browsers :browser/browsers)
(reg-root-key-sub :browser/options :browser/options)
(reg-root-key-sub :dapps/permissions :dapps/permissions)

;;stickers
(reg-root-key-sub :stickers/selected-pack :stickers/selected-pack)
(reg-root-key-sub :stickers/packs :stickers/packs)
(reg-root-key-sub :stickers/installed-packs :stickers/packs-installed)
(reg-root-key-sub :stickers/packs-owned :stickers/packs-owned)
(reg-root-key-sub :stickers/packs-pending :stickers/packs-pending)

;;mailserver
(reg-root-key-sub :mailserver/current-id :mailserver/current-id)
(reg-root-key-sub :mailserver/mailservers :mailserver/mailservers)
(reg-root-key-sub :mailserver.edit/mailserver :mailserver.edit/mailserver)
(reg-root-key-sub :mailserver/state :mailserver/state)
(reg-root-key-sub :mailserver/pending-requests :mailserver/pending-requests)
(reg-root-key-sub :mailserver/request-error? :mailserver/request-error)
(reg-root-key-sub :mailserver/fetching-gaps-in-progress :mailserver/fetching-gaps-in-progress)
(reg-root-key-sub :mailserver/gaps :mailserver/gaps)
(reg-root-key-sub :mailserver/ranges :mailserver/ranges)

;;contacts
(reg-root-key-sub ::contacts :contacts/contacts)
(reg-root-key-sub :contacts/current-contact-identity :contacts/identity)
(reg-root-key-sub :new-identity-error :contacts/new-identity-error)
(reg-root-key-sub :contacts/new-identity :contacts/new-identity)
(reg-root-key-sub :group/selected-contacts :group/selected-contacts)
;;wallet
(reg-root-key-sub :wallet :wallet)
(reg-root-key-sub :prices :prices)
(reg-root-key-sub :collectibles :collectibles)
(reg-root-key-sub :wallet/all-tokens :wallet/all-tokens)
(reg-root-key-sub :prices-loading? :prices-loading?)
(reg-root-key-sub :wallet.transactions :wallet.transactions)
(reg-root-key-sub :wallet/custom-token-screen :wallet/custom-token-screen)

;;ethereum
(reg-root-key-sub :ethereum/current-block :ethereum/current-block)

;;ens
(reg-root-key-sub :ens/registration :ens/registration)
(reg-root-key-sub :ens/names :ens/names)

;;signing
(reg-root-key-sub :signing/tx :signing/tx)
(reg-root-key-sub :signing/sign :signing/sign)
(reg-root-key-sub :signing/edit-fee :signing/edit-fee)

;;intro-wizard
(reg-root-key-sub :intro-wizard :intro-wizard)

(reg-root-key-sub :popover/popover :popover/popover)
(reg-root-key-sub :generate-account :generate-account)

;;GENERAL ==============================================================================================================

(re-frame/reg-sub
 :connection-stats
 :<- [:desktop/desktop]
 (fn [desktop _]
   (get desktop :debug-metrics)))

(re-frame/reg-sub
 :settings/logging-enabled
 :<- [:desktop/desktop]
 (fn [desktop _]
   (get desktop :logging-enabled false)))

(re-frame/reg-sub
 :current-network
 :<- [:networks/networks]
 :<- [:networks/current-network]
 (fn [[networks current-network]]
   (when-let [network (get networks current-network)]
     (assoc network
            :rpc-network? (get-in network [:config :UpstreamConfig :Enabled])))))

(re-frame/reg-sub
 :chain-name
 :<- [:current-network]
 (fn [network]
   (ethereum/network->chain-name network)))

(re-frame/reg-sub
 :chain-id
 :<- [:current-network]
 (fn [network]
   (ethereum/network->chain-id network)))

(re-frame/reg-sub
 :mainnet?
 :<- [:chain-id]
 (fn [chain-id]
   (= 1 chain-id)))

(re-frame/reg-sub
 :network-name
 :<- [:current-network]
 (fn [network]
   (:name network)))

(re-frame/reg-sub
 :disconnected?
 :<- [:peers-count]
 (fn [peers-count]
   (zero? peers-count)))

(re-frame/reg-sub
 :offline?
 :<- [:network-status]
 :<- [:sync-state]
 :<- [:disconnected?]
 (fn [[network-status sync-state disconnected?]]
   (or disconnected?
       (= network-status :offline)
       (= sync-state :offline))))

(re-frame/reg-sub
 :syncing?
 :<- [:sync-state]
 (fn [sync-state]
   (#{:pending :in-progress} sync-state)))

(re-frame/reg-sub
 :dimensions/window-width
 :<- [:dimensions/window]
 :width)

(re-frame/reg-sub
 :dimensions/window-height
 :<- [:dimensions/window]
 :height)

(re-frame/reg-sub
 :get-screen-params
 :<- [:screen-params]
 :<- [:view-id]
 (fn [[params view-id-db] [_ view-id]]
   (get params (or view-id view-id-db))))

(re-frame/reg-sub
 :can-navigate-back?
 :<- [:navigation-stack]
 (fn [stack]
   (> (count stack) 1)))

(re-frame/reg-sub
 :delete-swipe-position
 :<- [:animations]
 (fn [animations [_ type item-id]]
   (get-in animations [type item-id :delete-swiped])))

(re-frame/reg-sub
 :search/filter
 :<- [:ui/search]
 (fn [search]
   (get search :filter)))

(defn- node-version [web3-node-version]
  (str "status-go v" (or web3-node-version "N/A") ""))

(def app-short-version
  (let [version (if platform/desktop? build/version build/build-no)]
    (str build/version " (" version ")")))

(re-frame/reg-sub
 :get-app-version
 :<- [:web3-node-version]
 (fn [web3-node-version]
   (str app-short-version "; " (node-version web3-node-version))))

(re-frame/reg-sub
 :get-app-short-version
 (fn [db] app-short-version))

(re-frame/reg-sub
 :get-app-node-version
 :<- [:web3-node-version]
 node-version)

(re-frame/reg-sub
 :my-profile/recovery
 :<- [:my-profile/seed]
 (fn [seed]
   (or seed {:step :intro})))

(re-frame/reg-sub
 :bottom-sheet
 :<- [:bottom-sheet/show?]
 :<- [:bottom-sheet/view]
 (fn [[show? view]]
   {:show? show?
    :view  view}))

(re-frame/reg-sub
 :is-contact-selected?
 :<- [:group/selected-contacts]
 (fn [selected-contacts [_ element]]
   (-> selected-contacts
       (contains? element))))

(re-frame/reg-sub
 :is-participant-selected?
 :<- [:selected-participants]
 (fn [selected-participants [_ element]]
   (-> selected-participants
       (contains? element))))

(re-frame/reg-sub
 :ethereum/chain-keyword
 :<- [:current-network]
 (fn [network]
   (ethereum/network->chain-keyword network)))

(re-frame/reg-sub
 :ethereum/native-currency
 :<- [:ethereum/chain-keyword]
 (fn [chain-keyword]
   (tokens/native-currency chain-keyword)))

;;MULTIACCOUNT ==============================================================================================================

(re-frame/reg-sub
 :multiaccount/public-key
 :<- [:multiaccount]
 (fn [{:keys [public-key]}]
   public-key))

(re-frame/reg-sub
 :multiaccount/default-address
 :<- [:multiaccount]
 (fn [{:keys [accounts]}]
   (:address (ethereum/get-default-account accounts))))

(re-frame/reg-sub
 :sign-in-enabled?
 :<- [:multiaccounts/login]
 (fn [{:keys [password]}]
   (spec/valid? ::multiaccounts.db/password
                (security/safe-unmask-data password))))

(re-frame/reg-sub
 :settings/current-fleet
 :<- [:multiaccount-settings]
 (fn [sett]
   (fleet/current-fleet-sub sett)))

(re-frame/reg-sub
 :multiaccount-settings
 :<- [:multiaccount]
 (fn [acc]
   (get acc :settings)))

(re-frame/reg-sub
 :latest-block-number
 (fn [{:node/keys [latest-block-number]} _]
   (if latest-block-number latest-block-number 0)))

(re-frame/reg-sub
 :settings/current-log-level
 :<- [:multiaccount-settings]
 (fn [sett]
   (or (get sett :log-level)
       config/log-level-status-go)))

;;CHAT ==============================================================================================================

(re-frame/reg-sub
 :get-collectible-token
 :<- [:collectibles]
 (fn [collectibles [_ {:keys [symbol token]}]]
   (get-in collectibles [(keyword symbol) (js/parseInt token)])))

(re-frame/reg-sub
 ::show-suggestions-view?
 :<- [:chats/current-chat-ui-prop :show-suggestions?]
 :<- [:chats/current-chat]
 :<- [:chats/all-available-commands]
 (fn [[show-suggestions? {:keys [input-text]} commands]]
   (and (or show-suggestions?
            (commands.input/starts-as-command? (string/trim (or input-text ""))))
        (seq commands))))

(re-frame/reg-sub
 ::show-suggestions?
 :<- [::show-suggestions-view?]
 :<- [:chats/selected-chat-command]
 (fn [[show-suggestions-box? selected-command]]
   (and show-suggestions-box? (not selected-command))))

(re-frame/reg-sub
 ::get-commands-for-chat
 :<- [:chats/id->command]
 :<- [::access-scope->command-id]
 :<- [:chats/current-chat]
 (fn [[id->command access-scope->command-id chat]]
   (commands/chat-commands id->command access-scope->command-id chat)))

(re-frame/reg-sub
 :chats/chat
 :<- [:chats/active-chats]
 (fn [chats [_ chat-id]]
   (get chats chat-id)))

(re-frame/reg-sub
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

(re-frame/reg-sub
 :chats/current-chat-ui-props
 :<- [::chat-ui-props]
 :<- [:chats/current-chat-id]
 (fn [[chat-ui-props id]]
   (get chat-ui-props id)))

(re-frame/reg-sub
 :chats/current-chat-ui-prop
 :<- [:chats/current-chat-ui-props]
 (fn [ui-props [_ prop]]
   (get ui-props prop)))

(re-frame/reg-sub
 :chats/validation-messages
 :<- [:chats/current-chat-ui-props]
 (fn [ui-props]
   (some-> ui-props :validation-messages)))

(re-frame/reg-sub
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

(re-frame/reg-sub
 :chats/active-chats
 :<- [:contacts/contacts]
 :<- [::chats]
 :<- [:multiaccount]
 (fn [[contacts chats multiaccount]]
   (chat.db/active-chats contacts chats multiaccount)))

(defn enrich-current-one-to-one-chat
  [{:keys [contact] :as current-chat} my-public-key ttt-settings
   chain-keyword prices currency]
  (let [{:keys [tribute-to-talk]} contact
        {:keys [disabled? snt-amount message]} tribute-to-talk
        whitelisted-by? (whitelist/whitelisted-by? contact)
        loading?        (and (not whitelisted-by?)
                             (not tribute-to-talk))
        show-input?     (or whitelisted-by?
                            disabled?)
        token           (case chain-keyword
                          :mainnet :SNT
                          :STT)
        tribute-status  (if loading?
                          :loading
                          (tribute-to-talk.db/tribute-status contact))
        tribute-label   (tribute-to-talk.db/status-label tribute-status snt-amount)]

    (cond-> (assoc current-chat
                   :tribute-to-talk/tribute-status tribute-status
                   :tribute-to-talk/tribute-label tribute-label)

      (#{:required :pending :paid} tribute-status)
      (assoc :tribute-to-talk/snt-amount
             (tribute-to-talk.db/from-wei snt-amount)
             :tribute-to-talk/message
             message
             :tribute-to-talk/fiat-amount   (if snt-amount
                                              (money/fiat-amount-value
                                               snt-amount
                                               token
                                               (-> currency :code keyword)
                                               prices)
                                              "0")
             :tribute-to-talk/fiat-currency (:code currency)
             :tribute-to-talk/token         (str " " (name token)))

      (tribute-to-talk.db/enabled? ttt-settings)
      (assoc :tribute-to-talk/received? (tribute-to-talk.db/tribute-received?
                                         contact))

      (= tribute-status :required)
      (assoc :tribute-to-talk/on-share-my-profile
             #(re-frame/dispatch
               [:profile/share-profile-link my-public-key]))

      show-input?
      (assoc :show-input? true))))

(defn enrich-current-chat
  [{:keys [messages chat-id might-have-join-time-messages?] :as chat}
   ranges height input-height]
  (assoc chat
         :height height
         :input-height input-height
         :range
         (get ranges chat-id)
         :intro-status
         (if might-have-join-time-messages?
           :loading
           (if (empty? messages)
             :empty
             :messages))))

(re-frame/reg-sub
 :chats/current-chat
 :<- [:chats/active-chats]
 :<- [:chats/current-chat-id]
 :<- [:multiaccount/public-key]
 :<- [:mailserver/ranges]
 :<- [:chats/content-layout-height]
 :<- [:chats/current-chat-ui-prop :input-height]
 :<- [:tribute-to-talk/settings]
 :<- [:ethereum/chain-keyword]
 :<- [:prices]
 :<- [:wallet/currency]
 (fn [[chats current-chat-id my-public-key ranges height
       input-height ttt-settings chain-keyword prices currency]]
   (let [{:keys [group-chat contact messages]
          :as current-chat}
         (get chats current-chat-id)]
     (when current-chat
       (cond-> (enrich-current-chat current-chat ranges height input-height)
         (empty? messages)
         (assoc :universal-link
                (links/generate-link :public-chat :external current-chat-id))

         (chat.models/public-chat? current-chat)
         (assoc :show-input? true)

         (and (chat.models/group-chat? current-chat)
              (group-chats.db/joined? my-public-key current-chat))
         (assoc :show-input? true)

         (not group-chat)
         (enrich-current-one-to-one-chat my-public-key ttt-settings
                                         chain-keyword prices currency))))))

(re-frame/reg-sub
 :chats/current-chat-message
 :<- [:chats/current-chat]
 (fn [{:keys [messages]} [_ message-id]]
   (get messages message-id)))

(re-frame/reg-sub
 :chats/current-chat-messages
 :<- [:chats/current-chat]
 (fn [{:keys [messages]}]
   (or messages {})))

(re-frame/reg-sub
 :chats/current-chat-message-groups
 :<- [:chats/current-chat]
 (fn [{:keys [message-groups]}]
   (or message-groups {})))

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
 :<- [:chats/messages-gaps]
 :<- [:chats/range]
 :<- [:chats/all-loaded?]
 :<- [:chats/public?]
 (fn [[messages message-groups messages-gaps range all-loaded? public?]]
   (-> (chat.db/sort-message-groups message-groups messages)
       (chat.db/messages-with-datemarks
        messages messages-gaps range all-loaded? public?)
       chat.db/messages-stream)))

(re-frame/reg-sub
 :chats/current-chat-intro-status
 :<- [:chats/current-chat]
 :<- [:chats/current-chat-messages]
 (fn [[{:keys [might-have-join-time-messages?]} messages]]
   (if might-have-join-time-messages?
     :loading
     (if (empty? messages)
       :empty
       :messages))))

(re-frame/reg-sub
 :chats/available-commands
 :<- [::get-commands-for-chat]
 :<- [:chats/current-chat]
 (fn [[commands chat]]
   (chat.db/available-commands commands chat)))

(re-frame/reg-sub
 :chats/all-available-commands
 :<- [::get-commands-for-chat]
 (fn [commands]
   (chat.db/map->sorted-seq commands)))

(re-frame/reg-sub
 :chats/selected-chat-command
 :<- [:chats/current-chat]
 :<- [:chats/current-chat-ui-prop :selection]
 :<- [::get-commands-for-chat]
 (fn [[{:keys [input-text]} selection commands]]
   (commands.input/selected-chat-command input-text selection commands)))

(re-frame/reg-sub
 :chats/input-placeholder
 :<- [:chats/current-chat]
 :<- [:chats/selected-chat-command]
 (fn [[{:keys [input-text]} {:keys [params current-param-position cursor-in-the-end?]}]]
   (when (and cursor-in-the-end? (string/ends-with? (or input-text "") chat.constants/spacing-char))
     (get-in params [current-param-position :placeholder]))))

(re-frame/reg-sub
 :chats/parameter-box
 :<- [:chats/current-chat]
 :<- [:chats/selected-chat-command]
 (fn [[_ {:keys [current-param-position params]}]]
   (when (and params current-param-position)
     (get-in params [current-param-position :suggestions]))))

(re-frame/reg-sub
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

(re-frame/reg-sub
 :chats/unviewed-messages-count
 (fn [[_ chat-id]]
   (re-frame/subscribe [:chats/chat chat-id]))
 (fn [{:keys [unviewed-messages-count]}]
   unviewed-messages-count))

(re-frame/reg-sub
 :chats/photo-path
 :<- [:contacts/contacts]
 :<- [:multiaccount]
 (fn [[contacts multiaccount] [_ id]]
   (or (:photo-path (contacts id))
       (when (= id (:public-key multiaccount))
         (:photo-path multiaccount)))))

(re-frame/reg-sub
 :chats/unread-messages-number
 :<- [:chats/active-chats]
 (fn [chats _]
   (apply + (map :unviewed-messages-count (vals chats)))))

(re-frame/reg-sub
 :chats/cooldown-enabled?
 :<- [:chats/current-chat]
 :<- [::cooldown-enabled?]
 (fn [[{:keys [public?]} cooldown-enabled?]]
   (and public?
        cooldown-enabled?)))

(re-frame/reg-sub
 :chats/reply-message
 :<- [:chats/current-chat]
 (fn [{:keys [metadata messages]}]
   (get messages (get-in metadata [:responding-to-message :message-id]))))

(re-frame/reg-sub
 :public-chat.new/topic-error-message
 :<- [:public-group-topic]
 (fn [topic]
   (when-not (or (empty? topic)
                 (db/valid-topic? topic))
     (i18n/label :topic-name-error))))

(defn filter-selected-contacts
  [selected-contacts contacts]
  (filter #(contact.db/added? (contacts %)) selected-contacts))

(re-frame/reg-sub
 :selected-contacts-count
 :<- [:group/selected-contacts]
 :<- [:contacts/contacts]
 (fn [[selected-contacts contacts]]
   (count (filter-selected-contacts selected-contacts contacts))))

(re-frame/reg-sub
 :selected-participants-count
 :<- [:selected-participants]
 (fn [selected-participants]
   (count selected-participants)))

(defn filter-contacts [selected-contacts active-contacts]
  (filter #(selected-contacts (:public-key %)) active-contacts))

(re-frame/reg-sub
 :selected-group-contacts
 :<- [:group/selected-contacts]
 :<- [:contacts/active]
 (fn [[selected-contacts active-contacts]]
   (filter-contacts selected-contacts active-contacts)))

(re-frame/reg-sub
 :chats/transaction-status
 ;;TODO address here for transactions
 :<- [:wallet/transactions]
 :<- [:ethereum/current-block]
 (fn [[transactions current-block] [_ hash]]
   (when-let [transaction (get transactions hash)]
     {:exists? true
      :confirmed?
      (-> transaction
          (wallet.db/get-confirmations current-block)
          (>= transactions/confirmations-count-threshold))})))

;;BOOTNODES ============================================================================================================

(re-frame/reg-sub
 :settings/bootnodes-enabled
 :<- [:multiaccount]
 :<- [:networks/current-network]
 (fn [{:keys [settings]} current-network]
   (get-in settings [:bootnodes current-network])))

(re-frame/reg-sub
 :settings/network-bootnodes
 :<- [:multiaccount]
 :<- [:networks/current-network]
 (fn [multiaccount current-network]
   (get-in multiaccount [:bootnodes current-network])))

(re-frame/reg-sub
 :get-manage-bootnode
 :<- [:bootnodes/manage]
 (fn [manage]
   manage))

(re-frame/reg-sub
 :manage-bootnode-validation-errors
 :<- [:get-manage-bootnode]
 (fn [manage]
   (set (keep
         (fn [[k {:keys [error]}]]
           (when error k))
         manage))))

;;BROWSER ==============================================================================================================

(re-frame/reg-sub
 :browser/browsers
 :<- [:browsers]
 (fn [browsers]
   (reduce (fn [acc [k browser]]
             (update acc k assoc :url (browser/get-current-url browser)))
           browsers
           browsers)))

(re-frame/reg-sub
 :browser/browsers-vals
 :<- [:browser/browsers]
 (fn [browsers]
   (sort-by :timestamp > (vals browsers))))

(re-frame/reg-sub
 :get-current-browser
 :<- [:browser/options]
 :<- [:browser/browsers]
 (fn [[options browsers]]
   (let [browser (get browsers (:browser-id options))]
     (assoc browser :secure? (browser/secure? browser options)))))

;;STICKERS =============================================================================================================

(re-frame/reg-sub
 :stickers/installed-packs-vals
 :<- [:stickers/installed-packs]
 (fn [packs]
   (vals packs)))

(re-frame/reg-sub
 :stickers/all-packs
 :<- [:stickers/packs]
 :<- [:stickers/installed-packs]
 :<- [:stickers/packs-owned]
 :<- [:stickers/packs-pending]
 (fn [[packs installed owned pending]]
   (map (fn [{:keys [id] :as pack}]
          (cond-> pack
            (get installed id) (assoc :installed true)
            (get owned id) (assoc :owned true)
            (get pending id) (assoc :pending true)))
        (vals packs))))

(re-frame/reg-sub
 :stickers/get-current-pack
 :<- [:get-screen-params]
 :<- [:stickers/all-packs]
 (fn [[{:keys [id]} packs]]
   (first (filter #(= (:id %) id) packs))))

(defn find-pack-id-for-hash [sticker-uri packs]
  (some (fn [{:keys [stickers id]}]
          (when (some #(= sticker-uri (:hash %)) stickers)
            id))
        packs))

(re-frame/reg-sub
 :stickers/recent
 :<- [:multiaccount]
 :<- [:stickers/installed-packs-vals]
 (fn [[{:keys [:stickers/recent-stickers]} packs]]
   (map (fn [hash] {:hash hash :pack (find-pack-id-for-hash hash packs)}) recent-stickers)))

(re-frame/reg-sub
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

;;PAIRING ==============================================================================================================


(re-frame/reg-sub
 :pairing/installations
 :<- [:get-pairing-installations]
 :<- [:pairing/installation-id]
 (fn [[installations installation-id]]
   (->> installations
        vals
        (pairing/sort-installations installation-id))))

(re-frame/reg-sub
 :pairing/installation-id
 :<- [:multiaccount]
 :installation-id)

(re-frame/reg-sub
 :pairing/installation-name
 :<- [:multiaccount]
 (fn [multiaccount] (:installation-name multiaccount)))

;;PROFILE ==============================================================================================================

(re-frame/reg-sub
 :get-profile-unread-messages-number
 :<- [:multiaccount]
 (fn [{:keys [seed-backed-up? mnemonic]}]
   (if (or seed-backed-up? (string/blank? mnemonic)) 0 1)))

;;WALLET ==============================================================================================================

(re-frame/reg-sub
 :balance
 :<- [:wallet]
 (fn [wallet [_ address]]
   (get-in wallet [:accounts address :balance])))

(re-frame/reg-sub
 :balance-default
 :<- [:wallet]
 :<- [:multiaccount]
 (fn [[wallet {:keys [accounts]}]]
   (get-in wallet [:accounts (:address (ethereum/get-default-account accounts)) :balance])))

(re-frame/reg-sub
 :balances
 :<- [:wallet]
 (fn [wallet]
   (map :balance (vals (:accounts wallet)))))

(re-frame/reg-sub
 :price
 :<- [:prices]
 (fn [prices [_ fsym tsym]]
   (get-in prices [fsym tsym :price])))

(re-frame/reg-sub
 :last-day
 :<- [:prices]
 (fn [prices [_ fsym tsym]]
   (get-in prices [fsym tsym :last-day])))

(re-frame/reg-sub
 :wallet.settings/currency
 :<- [:multiaccount-settings]
 (fn [settings]
   (or (get-in settings [:wallet :currency]) :usd)))

(defn- get-balance-total-value
  [balance prices currency token->decimals]
  (reduce-kv (fn [acc symbol value]
               (if-let [price (get-in prices [symbol currency :price])]
                 (+ acc (or (some-> (money/internal->formatted value symbol (token->decimals symbol))
                                    (money/crypto->fiat price)
                                    .toNumber)
                            0))
                 acc)) 0 balance))

(re-frame/reg-sub
 :portfolio-value
 :<- [:balances]
 :<- [:prices]
 :<- [:wallet/currency]
 :<- [:ethereum/chain-keyword]
 :<- [:wallet/all-tokens]
 (fn [[balances prices currency chain all-tokens]]
   (if (and balances prices)
     (let [assets              (tokens/tokens-for all-tokens chain)
           token->decimals     (into {} (map #(vector (:symbol %) (:decimals %)) assets))
           currency-key        (-> currency :code keyword)
           balance-total-value (apply + (map #(get-balance-total-value % prices currency-key token->decimals) balances))]
       (if (pos? balance-total-value)
         (-> balance-total-value
             (money/with-precision 2)
             str
             (i18n/format-currency (:code currency) false))
         "0"))
     "...")))

(re-frame/reg-sub
 :account-portfolio-value
 (fn [[_ address] _]
   [(re-frame/subscribe [:balance address])
    (re-frame/subscribe [:prices])
    (re-frame/subscribe [:wallet/currency])
    (re-frame/subscribe [:ethereum/chain-keyword])
    (re-frame/subscribe [:wallet/all-tokens])])
 (fn [[balance prices currency chain all-tokens]]
   (if (and balance prices)
     (let [assets              (tokens/tokens-for all-tokens chain)
           token->decimals     (into {} (map #(vector (:symbol %) (:decimals %)) assets))
           currency-key        (-> currency :code keyword)
           balance-total-value (get-balance-total-value balance prices currency-key token->decimals)]
       (if (pos? balance-total-value)
         (-> balance-total-value
             (money/with-precision 2)
             str
             (i18n/format-currency (:code currency) false))
         "0"))
     "...")))

(re-frame/reg-sub
 :wallet/sorted-chain-tokens
 :<- [:wallet/all-tokens]
 :<- [:ethereum/chain-keyword]
 (fn [[all-tokens chain]]
   (tokens/sorted-tokens-for all-tokens chain)))

(re-frame/reg-sub
 :wallet/grouped-chain-tokens
 :<- [:wallet/sorted-chain-tokens]
 :<- [:wallet/visible-tokens-symbols]
 (fn [[all-tokens visible-tokens]]
   (let [vt-set (set visible-tokens)]
     (group-by :custom? (map #(assoc % :checked? (boolean (get vt-set (keyword (:symbol %))))) all-tokens)))))

(re-frame/reg-sub
 :wallet/error-message
 :<- [:wallet]
 (fn [wallet]
   (or (get-in wallet [:errors :balance-update])
       (get-in wallet [:errors :prices-update]))))

(re-frame/reg-sub
 :wallet/visible-tokens-symbols
 :<- [:ethereum/chain-keyword]
 :<- [:multiaccount]
 (fn [[chain current-multiaccount]]
   (get-in current-multiaccount [:settings :wallet :visible-tokens chain])))

(re-frame/reg-sub
 :wallet/visible-assets
 :<- [:ethereum/chain-keyword]
 :<- [:wallet/visible-tokens-symbols]
 :<- [:wallet/all-tokens]
 (fn [[chain visible-tokens-symbols all-tokens]]
   (conj (filter #(contains? visible-tokens-symbols (:symbol %))
                 (tokens/sorted-tokens-for all-tokens chain))
         (tokens/native-currency chain))))

(re-frame/reg-sub
 :wallet/visible-assets-with-amount
 (fn [[_ address] _]
   [(re-frame/subscribe [:balance address])
    (re-frame/subscribe [:wallet/visible-assets])])
 (fn [[balance visible-assets]]
   (map #(assoc % :amount (get balance (:symbol %))) visible-assets)))

(defn update-value [prices currency]
  (fn [{:keys [symbol decimals amount] :as token}]
    (let [price (get-in prices [symbol (-> currency :code keyword) :price])]
      (assoc token
             :price price
             :value (when (and amount price)
                      (-> (money/internal->formatted amount symbol decimals)
                          (money/crypto->fiat price)
                          (money/with-precision 2)
                          str
                          (i18n/format-currency (:code currency))))))))

(re-frame/reg-sub
 :wallet/visible-assets-with-values
 (fn [[_ address] _]
   [(re-frame/subscribe [:wallet/visible-assets-with-amount address])
    (re-frame/subscribe [:prices])
    (re-frame/subscribe [:wallet/currency])])
 (fn [[assets prices currency]]
   (let [{:keys [tokens nfts]} (group-by #(if (:nft? %) :nfts :tokens) assets)
         tokens-with-values (map (update-value prices currency) tokens)]
     {:tokens tokens-with-values
      :nfts   nfts})))

(defn get-asset-amount [balances sym]
  (reduce #(if-let [bl (get %2 sym)]
             (.plus %1 bl)
             %1)
          (money/bignumber 0)
          balances))

(re-frame/reg-sub
 :wallet/all-visible-assets-with-amount
 :<- [:balances]
 :<- [:wallet/visible-assets]
 (fn [[balances visible-assets]]
   (map #(assoc % :amount (get-asset-amount balances (:symbol %))) visible-assets)))

(re-frame/reg-sub
 :wallet/all-visible-assets-with-values
 :<- [:wallet/all-visible-assets-with-amount]
 :<- [:prices]
 :<- [:wallet/currency]
 (fn [[assets prices currency]]
   (let [{:keys [tokens nfts]} (group-by #(if (:nft? %) :nfts :tokens) assets)
         tokens-with-values (map (update-value prices currency) tokens)]
     {:tokens tokens-with-values
      :nfts   nfts})))

(re-frame/reg-sub
 :wallet/transferrable-assets-with-amount
 (fn [[_ address]]
   (re-frame/subscribe [:wallet/visible-assets-with-amount address]))
 (fn [all-assets]
   (filter #(not (:nft? %)) all-assets)))

(re-frame/reg-sub
 :wallet/currency
 :<- [:wallet.settings/currency]
 (fn [currency-id]
   (get constants/currencies currency-id)))

;;WALLET TRANSACTIONS ==================================================================================================

(re-frame/reg-sub
 :wallet/transactions
 :<- [:wallet]
 (fn [wallet [_ address]]
   (get-in wallet [:accounts address :transactions])))

(re-frame/reg-sub
 :wallet/filters
 :<- [:wallet]
 (fn [wallet]
   (get wallet :filters)))

(defn enrich-transaction
  [{:keys [type to from value token] :as transaction}
   contacts native-currency]
  (let [[contact-address key-contact key-wallet]
        (if (= type :inbound)
          [from :from-contact :to-wallet]
          [to :to-contact :from-wallet])
        wallet  (i18n/label :main-wallet)
        contact (get contacts contact-address)
        {:keys [symbol-display symbol decimals] :as asset}
        (or token native-currency)
        amount-text   (if value
                        (wallet.utils/format-amount value decimals)
                        "...")
        currency-text (when asset
                        (clojure.core/name (or symbol-display symbol)))]
    (cond-> transaction
      contact (assoc key-contact (:name contact))
      :always (assoc key-wallet wallet
                     :amount-text    amount-text
                     :currency-text  currency-text))))

(re-frame/reg-sub
 :wallet.transactions/transactions
 (fn [[_ address] _]
   [(re-frame/subscribe [:wallet/transactions address])
    (re-frame/subscribe [:contacts/contacts-by-address])
    (re-frame/subscribe [:ethereum/native-currency])])
 (fn [[transactions contacts native-currency]]
   (reduce (fn [acc [hash transaction]]
             (assoc acc
                    hash
                    (enrich-transaction transaction contacts native-currency))) ;;TODO this doesn't look good for performance, we need to calculate this only once for each transaction
           {}
           transactions)))

(re-frame/reg-sub
 :wallet.transactions/all-filters?
 :<- [:wallet/filters]
 (fn [filters]
   (= wallet.db/default-wallet-filters
      filters)))

(def filters-labels
  {:inbound  (i18n/label :t/incoming)
   :outbound (i18n/label :t/outgoing)
   :pending  (i18n/label :t/pending)
   :failed   (i18n/label :t/failed)})

(re-frame/reg-sub
 :wallet.transactions/filters
 :<- [:wallet/filters]
 (fn [filters]
   (map (fn [id]
          (let [checked? (filters id)]
            {:id id
             :label (filters-labels id)
             :checked? checked?
             :on-touch #(if checked?
                          (re-frame/dispatch [:wallet.transactions/remove-filter id])
                          (re-frame/dispatch [:wallet.transactions/add-filter id]))}))
        wallet.db/default-wallet-filters)))

(re-frame/reg-sub
 :wallet.transactions.filters/screen
 :<- [:wallet.transactions/filters]
 :<- [:wallet.transactions/all-filters?]
 (fn [[filters all-filters?]]
   {:all-filters? all-filters?
    :filters filters
    :on-touch-select-all (when-not all-filters?
                           #(re-frame/dispatch
                             [:wallet.transactions/add-all-filters]))}))

(defn- enrich-transaction-for-list
  [filters
   {:keys [type from-contact from to-contact to hash timestamp] :as transaction}
   address]
  (when (filters type)
    (assoc (case type
             :inbound
             (assoc transaction
                    :label (i18n/label :t/from)
                    :contact-accessibility-label :sender-text
                    :address-accessibility-label :sender-address-text
                    :contact from-contact
                    :address from)
             (assoc transaction
                    :label (i18n/label :t/to)
                    :contact-accessibility-label :recipient-name-text
                    :address-accessibility-label :recipient-address-text
                    :contact to-contact
                    :address to))
           :time-formatted (datetime/timestamp->time timestamp)
           :on-touch-fn #(re-frame/dispatch [:wallet.ui/show-transaction-details hash address]))))

(defn- group-transactions-by-date
  [transactions]
  (->> transactions
       (group-by #(datetime/timestamp->date-key (:timestamp %)))
       (sort-by key >)
       (map (fn [[date-key transactions]]
              {:title (datetime/timestamp->mini-date (:timestamp (first transactions)))
               :key   date-key
               :data  (sort-by :timestamp > transactions)}))))

(re-frame/reg-sub
 :wallet.transactions.history/screen
 (fn [[_ address] _]
   [(re-frame/subscribe [:wallet.transactions/transactions address])
    (re-frame/subscribe [:wallet/filters])
    (re-frame/subscribe [:wallet.transactions/all-filters?])])
 (fn [[transactions filters all-filters?] [_ address]]
   {:all-filters? all-filters?
    :transaction-history-sections
    (->> transactions
         vals
         (keep #(enrich-transaction-for-list filters % address))
         (group-transactions-by-date))}))

(re-frame/reg-sub
 :wallet.transactions.details/current-transaction
 (fn [[_ hash address] _]
   [(re-frame/subscribe [:wallet.transactions/transactions address])
    (re-frame/subscribe [:ethereum/native-currency])
    (re-frame/subscribe [:ethereum/chain-keyword])])
 (fn [[transactions native-currency chain-keyword] [_ hash _]]
   (let [{:keys [gas-used gas-price hash timestamp type token value]
          :as transaction}
         (get transactions hash)
         native-currency-text (name (or (:symbol-display native-currency)
                                        (:symbol native-currency)))]
     (when transaction
       (merge transaction
              {:gas-price-eth  (if gas-price
                                 (money/wei->str :eth
                                                 gas-price
                                                 native-currency-text)
                                 "-")
               :gas-price-gwei (if gas-price
                                 (money/wei->str :gwei
                                                 gas-price)
                                 "-")
               :date           (datetime/timestamp->long-date timestamp)}
              (if (= type :unsigned)
                {:block     (i18n/label :not-applicable)
                 :cost      (i18n/label :not-applicable)
                 :gas-limit (i18n/label :not-applicable)
                 :gas-used  (i18n/label :not-applicable)
                 :nonce     (i18n/label :not-applicable)
                 :hash      (i18n/label :not-applicable)}
                {:cost (when gas-used
                         (money/wei->str :eth
                                         (money/fee-value gas-used gas-price)
                                         native-currency-text))
                 :url  (transactions/get-transaction-details-url
                        chain-keyword
                        hash)}))))))

(re-frame/reg-sub
 :wallet.transactions.details/screen
 (fn [[_ hash address] _]
   [(re-frame/subscribe [:wallet.transactions.details/current-transaction hash address])
    (re-frame/subscribe [:ethereum/current-block])])
 (fn [[transaction current-block]]
   (let [confirmations (wallet.db/get-confirmations transaction
                                                    current-block)]
     (assoc transaction
            :confirmations confirmations
            :confirmations-progress
            (if (>= confirmations transactions/confirmations-count-threshold)
              100
              (* 100 (/ confirmations transactions/confirmations-count-threshold)))))))

;;WALLET SEND ==========================================================================================================

(re-frame/reg-sub
 ::send-transaction
 :<- [:wallet]
 (fn [wallet]
   (:send-transaction wallet)))

(re-frame/reg-sub
 :wallet.send/symbol
 :<- [::send-transaction]
 (fn [send-transaction]
   (:symbol send-transaction)))

(re-frame/reg-sub
 :wallet.send/camera-flashlight
 :<- [::send-transaction]
 (fn [send-transaction]
   (:camera-flashlight send-transaction)))

(defn check-sufficient-funds
  [{:keys [sufficient-funds?] :as transaction} balance symbol amount]
  (cond-> transaction
    (nil? sufficient-funds?)
    (assoc :sufficient-funds?
           (or (nil? amount)
               (money/sufficient-funds? amount (get balance symbol))))))

(defn check-sufficient-gas
  [transaction balance symbol amount]
  (assoc transaction :sufficient-gas?
         (or (nil? amount)
             (let [available-ether   (money/bignumber (get balance :ETH 0))
                   available-for-gas (if (= :ETH symbol)
                                       (.minus available-ether (money/bignumber amount))
                                       available-ether)]
               (money/sufficient-funds? (-> transaction
                                            :max-fee
                                            money/bignumber
                                            (money/formatted->internal :ETH 18))
                                        (money/bignumber available-for-gas))))))

(re-frame/reg-sub
 :wallet.send/transaction
 :<- [::send-transaction]
 :<- [:wallet]
 (fn [[{:keys [amount symbol from] :as transaction} wallet]]
   (let [balance (get-in wallet [:accounts from :balance])]
     (-> transaction
         (check-sufficient-funds balance symbol amount)
         (check-sufficient-gas balance symbol amount)))))

(re-frame/reg-sub
 :wallet/settings
 :<- [:wallet]
 (fn [{:keys [settings]}]
   (reduce-kv #(conj %1 %3) [] settings)))

(re-frame/reg-sub
 :wallet.request/transaction
 :<- [:wallet]
 :request-transaction)

(re-frame/reg-sub
 :screen-collectibles
 :<- [:collectibles]
 :<- [:get-screen-params]
 (fn [[collectibles {:keys [symbol]}]]
   (when-let [v (get collectibles symbol)]
     (mapv #(assoc (second %) :id (first %)) v))))

;;UI ==============================================================================================================

;;TODO this subscription looks super weird huge and with dispatches?
(re-frame/reg-sub
 :connectivity/status-properties
 :<- [:offline?]
 :<- [:disconnected?]
 :<- [:mailserver/connecting?]
 :<- [:mailserver/connection-error?]
 :<- [:mailserver/request-error?]
 :<- [:mailserver/fetching?]
 :<- [:network/type]
 :<- [:multiaccount]
 (fn [[offline? disconnected? mailserver-connecting? mailserver-connection-error?
       mailserver-request-error? mailserver-fetching? network-type multiaccount]]
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
                                (not (:syncing-on-mobile-network? multiaccount)))
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

(re-frame/reg-sub
 ::query-current-chat-contacts
 :<- [:chats/current-chat]
 :<- [:contacts/contacts]
 (fn [[chat contacts] [_ query-fn]]
   (contact.db/query-chat-contacts chat contacts query-fn)))

(re-frame/reg-sub
 :contacts/contacts
 :<- [::contacts]
 (fn [contacts]
   (contact.db/enrich-contacts contacts)))

(re-frame/reg-sub
 :contacts/active
 :<- [:contacts/contacts]
 (fn [contacts]
   (contact.db/get-active-contacts contacts)))

(re-frame/reg-sub
 :contacts/active-count
 :<- [:contacts/active]
 (fn [active-contacts]
   (count active-contacts)))

(re-frame/reg-sub
 :contacts/blocked
 :<- [:contacts/contacts]
 (fn [contacts]
   (->> contacts
        (filter (fn [[_ contact]]
                  (contact.db/blocked? contact)))
        (contact.db/sort-contacts))))

(re-frame/reg-sub
 :contacts/blocked-count
 :<- [:contacts/blocked]
 (fn [blocked-contacts]
   (count blocked-contacts)))

(re-frame/reg-sub
 :contacts/current-contact
 :<- [:contacts/contacts]
 :<- [:contacts/current-contact-identity]
 (fn [[contacts identity]]
   (or (contacts identity)
       (-> identity
           contact.db/public-key->new-contact
           contact.db/enrich-contact))))

(re-frame/reg-sub
 :contacts/contact-name-by-identity
 :<- [:contacts/contacts]
 :<- [:multiaccount]
 (fn [[contacts current-multiaccount] [_ identity]]
   (let [me? (= (:public-key current-multiaccount) identity)]
     (if me?
       (:name current-multiaccount)
       (:name (contacts identity))))))

(re-frame/reg-sub
 :contacts/all-contacts-not-in-current-chat
 :<- [::query-current-chat-contacts remove]
 (fn [contacts]
   (sort-by (comp clojure.string/lower-case :name) contacts)))

(re-frame/reg-sub
 :contacts/current-chat-contacts
 :<- [:chats/current-chat]
 :<- [:contacts/contacts]
 :<- [:multiaccount]
 (fn [[{:keys [contacts admins]} all-contacts current-multiaccount]]
   (contact.db/get-all-contacts-in-group-chat contacts admins all-contacts current-multiaccount)))

(re-frame/reg-sub
 :contacts/contacts-by-chat
 (fn [[_ _ chat-id] _]
   [(re-frame/subscribe [:chats/chat chat-id])
    (re-frame/subscribe [:contacts/contacts])])
 (fn [[chat all-contacts] [_ query-fn]]
   (contact.db/query-chat-contacts chat all-contacts query-fn)))

(re-frame/reg-sub
 :contacts/chat-photo
 (fn [[_ chat-id] _]
   [(re-frame/subscribe [:chats/chat chat-id])
    (re-frame/subscribe [:contacts/contacts-by-chat filter chat-id])])
 (fn [[chat contacts] [_ chat-id]]
   (when (and chat (not (:group-chat chat)))
     (cond
       (:photo-path chat)
       (:photo-path chat)

       (pos? (count contacts))
       (:photo-path (first contacts))

       :else
       (identicon/identicon chat-id)))))

(re-frame/reg-sub
 :contacts/contact-by-address
 :<- [:contacts/contacts]
 (fn [contacts [_ address]]
   (contact.db/find-contact-by-address contacts address)))

(re-frame/reg-sub
 :contacts/contacts-by-address
 :<- [:contacts/contacts]
 (fn [contacts]
   (reduce (fn [acc [_ {:keys [address] :as contact}]]
             (if address
               (assoc acc address contact)
               acc))
           {}
           contacts)))

;;MAILSERVER ===========================================================================================================

(re-frame/reg-sub
 :mailserver/connecting?
 :<- [:mailserver/state]
 (fn [state]
   (#{:connecting :added} state)))

(re-frame/reg-sub
 :mailserver/connection-error?
 :<- [:mailserver/state]
 (fn [state]
   (#{:error :disconnected} state)))

(re-frame/reg-sub
 :chats/fetching-gap-in-progress?
 :<- [:chats/current-chat-id]
 :<- [:mailserver/fetching-gaps-in-progress]
 (fn [[chat-id gaps] [_ ids]]
   (seq (select-keys (get gaps chat-id) ids))))

(re-frame/reg-sub
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

(re-frame/reg-sub
 :mailserver/fleet-mailservers
 :<- [:settings/current-fleet]
 :<- [:mailserver/mailservers]
 (fn [[current-fleet mailservers]]
   (current-fleet mailservers)))

(re-frame/reg-sub
 :mailserver.edit/connected?
 :<- [:mailserver.edit/mailserver]
 :<- [:mailserver/current-id]
 (fn [[mailserver current-mailserver-id]]
   (= (get-in mailserver [:id :value])
      current-mailserver-id)))

(re-frame/reg-sub
 :mailserver.edit/validation-errors
 :<- [:mailserver.edit/mailserver]
 (fn [mailserver]
   (set (keep
         (fn [[k {:keys [error]}]]
           (when error k))
         mailserver))))

(re-frame/reg-sub
 :mailserver/connected?
 :<- [:mailserver/state]
 :<- [:network-status]
 (fn [[mail-state network-status]]
   (let [connected? (= :connected mail-state)
         online?    (= :online network-status)]
     (and connected? online?))))

(re-frame/reg-sub
 :mailserver/preferred-id
 :<- [:multiaccount-settings]
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

(re-frame/reg-sub
 :search/filtered-chats
 :<- [:chats/active-chats]
 :<- [:search/filter]
 (fn [[chats search-filter]]
   (apply-filter search-filter chats extract-chat-attributes)))

;; TRIBUTE TO TALK
(re-frame/reg-sub
 :tribute-to-talk/settings
 :<- [:multiaccount-settings]
 :<- [:ethereum/chain-keyword]
 (fn [[settings chain-keyword]]
   (get-in settings [:tribute-to-talk chain-keyword])))

(re-frame/reg-sub
 :tribute-to-talk/screen-params
 :<- [:screen-params]
 (fn [screen-params]
   (get screen-params :tribute-to-talk)))

(re-frame/reg-sub
 :tribute-to-talk/profile
 :<- [:tribute-to-talk/settings]
 :<- [:tribute-to-talk/screen-params]
 (fn [[{:keys [seen? snt-amount]}
       {:keys [state unavailable?]}]]
   (let [state (or state (if snt-amount :completed :disabled))
         snt-amount (tribute-to-talk.db/from-wei snt-amount)]
     (when config/tr-to-talk-enabled?
       (if unavailable?
         {:subtext "Change network to enable Tribute to Talk"
          :active? false
          :icon :main-icons/tribute-to-talk
          :icon-color colors/gray}
         (cond-> {:new? (not seen?)}
           (and (not (and seen?
                          snt-amount
                          (#{:signing :pending :transaction-failed :completed} state))))
           (assoc :subtext (i18n/label :t/tribute-to-talk-desc))

           (#{:signing :pending} state)
           (assoc :activity-indicator {:animating true
                                       :color colors/blue}
                  :subtext (case state
                             :pending (i18n/label :t/pending-confirmation)
                             :signing (i18n/label :t/waiting-to-sign)))

           (= state :transaction-failed)
           (assoc :icon :main-icons/warning
                  :icon-color colors/red
                  :subtext (i18n/label :t/transaction-failed))

           (not (#{:signing :pending :transaction-failed} state))
           (assoc :icon :main-icons/tribute-to-talk)

           (and (= state :completed)
                (not-empty snt-amount))
           (assoc :accessory-value (str snt-amount " SNT"))))))))

(re-frame/reg-sub
 :tribute-to-talk/enabled?
 :<- [:tribute-to-talk/settings]
 (fn [settings]
   (tribute-to-talk.db/enabled? settings)))

(re-frame/reg-sub
 :tribute-to-talk/settings-ui
 :<- [:tribute-to-talk/settings]
 :<- [:tribute-to-talk/screen-params]
 :<- [:prices]
 :<- [:wallet/currency]
 (fn [[{:keys [seen? snt-amount message]
        :as settings}
       {:keys [step editing? state error]
        :or {step :intro}
        screen-snt-amount :snt-amount
        screen-message :message} prices currency]]
   (let [fiat-value (if snt-amount
                      (money/fiat-amount-value
                       snt-amount
                       :SNT
                       (-> currency :code keyword)
                       prices)
                      "0")]
     (cond-> {:seen? seen?
              :snt-amount (tribute-to-talk.db/from-wei snt-amount)
              :message message
              :enabled? (tribute-to-talk.db/enabled? settings)
              :error error
              :step step
              :state (or state (if snt-amount :completed :disabled))
              :editing? editing?
              :fiat-value (str "~" fiat-value " " (:code currency))}

       (= step :set-snt-amount)
       (assoc :snt-amount (str screen-snt-amount)
              :disable-button?
              (boolean (and (= step :set-snt-amount)
                            (or (string/blank? screen-snt-amount)
                                (#{"0" "0.0" "0.00"} screen-snt-amount)
                                (string/ends-with? screen-snt-amount ".")))))))))

;;ENS ==================================================================================================================

(re-frame/reg-sub
 :ens.stateofus/registrar
 :<- [:current-network]
 (fn [network]
   (let [chain (ethereum/network->chain-keyword network)]
     (get stateofus/registrars chain))))

(re-frame/reg-sub
 :multiaccount/usernames
 :<- [:multiaccount]
 (fn [multiaccount]
   (:usernames multiaccount)))

(re-frame/reg-sub
 :ens/preferred-name
 :<- [:multiaccount]
 (fn [multiaccount]
   (:preferred-name multiaccount)))

(re-frame/reg-sub
 :ens/show?
 :<- [:multiaccount]
 (fn [multiaccount]
   (:show-name? multiaccount)))

(re-frame/reg-sub
 :ens.registration/screen
 :<- [:ens/registration]
 :<- [:ens.stateofus/registrar]
 :<- [:multiaccount]
 (fn [[{:keys [custom-domain? username-candidate registering?] :as ens} registrar {:keys [accounts public-key]}]]
   {:state          (get-in ens [:states username-candidate])
    :registering?   registering?
    :username       username-candidate
    :custom-domain? (or custom-domain? false)
    :contract       registrar
    :address        (:address (ethereum/get-default-account accounts))
    :public-key     public-key}))

(re-frame/reg-sub
 :ens.name/screen
 :<- [:get-screen-params :ens-name-details]
 :<- [:ens/names]
 (fn [[name ens]]
   (let [{:keys [address public-key]} (get-in ens [:details name])]
     {:name       name
      :address    address
      :public-key public-key})))

(re-frame/reg-sub
 :ens.main/screen
 :<- [:multiaccount/usernames]
 :<- [:multiaccount]
 :<- [:ens/preferred-name]
 :<- [:ens/show?]
 (fn [[names multiaccount preferred-name show?]]
   {:names          names
    :multiaccount   multiaccount
    :preferred-name preferred-name
    :show?          (or show? false)}))

;;SIGNING =============================================================================================================

(re-frame/reg-sub
 :signing/fee
 :<- [:signing/tx]
 (fn [{:keys [gas gasPrice]}]
   (signing.gas/calculate-max-fee gas gasPrice)))

(re-frame/reg-sub
 :signing/phrase
 :<- [:multiaccount]
 (fn [{:keys [signing-phrase]}]
   signing-phrase))

(defn- too-precise-amount?
  "Checks if number has any extra digit beyond the allowed number of decimals.
  It does so by checking the number against its rounded value."
  [amount decimals]
  (let [bn (money/bignumber amount)]
    (not (.eq bn (.round bn decimals)))))

(defn get-amount-error [amount decimals]
  (when (and (not (empty? amount)) decimals)
    (let [normalized-amount (money/normalize amount)
          value             (money/bignumber normalized-amount)]
      (cond
        (not (money/valid? value))
        {:amount-error (i18n/label :t/validation-amount-invalid-number)}

        (too-precise-amount? normalized-amount decimals)
        {:amount-error (i18n/label :t/validation-amount-is-too-precise {:decimals decimals})}

        :else nil))))

(defn get-sufficient-funds-error
  [balance symbol amount]
  (when-not (money/sufficient-funds? amount (get balance symbol))
    {:amount-error (i18n/label :t/wallet-insufficient-funds)}))

(defn get-sufficient-gas-error
  [balance symbol amount gas gasPrice]
  (if (and gas gasPrice)
    (let [fee               (.times gas gasPrice)
          available-ether   (money/bignumber (get balance :ETH 0))
          available-for-gas (if (= :ETH symbol)
                              (.minus available-ether (money/bignumber amount))
                              available-ether)]
      (when-not (money/sufficient-funds? fee (money/bignumber available-for-gas))
        {:gas-error (i18n/label :t/wallet-insufficient-gas)}))
    {:gas-error (i18n/label :t/invalid-number)}))

(re-frame/reg-sub
 :signing/amount-errors
 (fn [[_ address] _]
   [(re-frame/subscribe [:signing/tx])
    (re-frame/subscribe [:balance address])])
 (fn [[{:keys [amount token gas gasPrice approve?]} balance]]
   (if (and amount token (not approve?))
     (let [amount-bn (money/formatted->internal (money/bignumber amount) (:symbol token) (:decimals token))
           amount-error (or (get-amount-error amount (:decimals token))
                            (get-sufficient-funds-error balance (:symbol token) amount-bn))]
       (or amount-error (get-sufficient-gas-error balance (:symbol token) amount-bn gas gasPrice)))
     (get-sufficient-gas-error balance nil nil gas gasPrice))))

;; NETWORK SETTINGS

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

(re-frame/reg-sub
 :get-networks
 :<- [:networks/networks]
 (fn [networks]
   (let [networks (map (label-networks constants/default-networks) (sort-by :name (vals networks)))
         types    [:mainnet :testnet :custom]]
     (zipmap
      types
      (map #(filter (filter-networks %) networks) types)))))

(re-frame/reg-sub
 :manage-network-valid?
 :<- [:networks/manage]
 (fn [manage]
   (not-any? :error (vals manage))))
