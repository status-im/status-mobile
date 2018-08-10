(ns status-im.ui.screens.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.utils.ethereum.core :as ethereum]
            status-im.chat.subs
            status-im.ui.screens.accounts.subs
            status-im.ui.screens.home.subs
            status-im.ui.screens.contacts.subs
            status-im.ui.screens.group.subs
            status-im.ui.screens.wallet.subs
            status-im.ui.screens.wallet.collectibles.subs
            status-im.ui.screens.wallet.request.subs
            status-im.ui.screens.wallet.send.subs
            status-im.ui.screens.wallet.transactions.subs
            status-im.ui.screens.network-settings.subs
            status-im.ui.screens.offline-messaging-settings.subs
            status-im.ui.screens.bootnodes-settings.subs
            status-im.ui.screens.currency-settings.subs
            status-im.ui.screens.browser.subs
            status-im.ui.screens.add-new.new-chat.subs
            status-im.ui.screens.add-new.new-public-chat.subs
            status-im.ui.screens.profile.subs))

(reg-sub :get
         (fn [db [_ k]]
           (get db k)))

(reg-sub :get-in
         (fn [db [_ path]]
           (get-in db path)))

(reg-sub :network
         :<- [:get-current-account]
         (fn [current-account]
           (get (:networks current-account) (:network current-account))))

(reg-sub :network-name :chain)

(reg-sub :sync-state :sync-state)
(reg-sub :network-status :network-status)
(reg-sub :peers-count :peers-count)
(reg-sub :mailserver-status :mailserver-status)

(reg-sub :fetching?
         (fn [db]
           (get db :inbox/fetching?)))

(reg-sub :offline?
         :<- [:network-status]
         :<- [:sync-state]
         (fn [[network-status sync-state]]
           (or (= network-status :offline)
               (= sync-state :offline))))

(reg-sub :disconnected?
         :<- [:peers-count]
         (fn [peers-count]
           (zero? peers-count)))

(reg-sub :mailserver-error?
         :<- [:mailserver-status]
         (fn [mailserver-status]
           (#{:error :disconnected} mailserver-status)))

(reg-sub :syncing?
         :<- [:sync-state]
         (fn [sync-state]
           (#{:pending :in-progress} sync-state)))

(reg-sub :tab-bar-visible?
         (fn [db _]
           (get db :tab-bar-visible?)))

(reg-sub :get-screen-params
         (fn [db [_ view-id]]
           (get-in db [:navigation/screen-params (or view-id (:view-id db))])))

(reg-sub :can-navigate-back?
         (fn [db]
           (> (count (:navigation-stack db)) 1)))

(reg-sub :delete-swipe-position
         (fn [db [_ item-id]]
           (let [item-animation (get-in db [:chat-animations item-id])]
             (if (some? item-animation) (:delete-swiped item-animation) nil))))

(reg-sub :get-current-account-network
         (fn [{:keys [network] :as db} [_]]
           (get-in db [:account/account :networks network])))

(reg-sub :dimensions/window
         (fn [db _]
           (get db :dimensions/window)))

(reg-sub :dimensions/window-width
         :<- [:dimensions/window]
         :width)
