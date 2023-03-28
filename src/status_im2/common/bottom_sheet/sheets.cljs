(ns status-im2.common.bottom-sheet.sheets
  (:require [utils.re-frame :as rf]
            [status-im.ui.screens.about-app.views :as about-app]
            [status-im.ui.screens.home.sheet.views :as home.sheet]
            [status-im.ui.screens.keycard.views :as keycard]
            [status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
            [status-im.ui.screens.multiaccounts.key-storage.views :as key-storage]
            [status-im.ui.screens.multiaccounts.recover.views :as recover.views]
            [status-im2.common.bottom-sheet.view :as bottom-sheet]
            [status-im2.contexts.chat.menus.pinned-messages.view :as pinned-messages-menu]
            [react-native.core :as rn]))

(defn bottom-sheet
  []
  (let [dismiss-bottom-sheet-callback (fn []
                                        (rf/dispatch [:bottom-sheet/hide])
                                        true)
        {:keys [show? view options]} (rf/sub [:bottom-sheet])
        {:keys [content]
         :as   opts}
        (cond-> {:visible? show?}
          (map? view)
          (merge view)

          (= view :mobile-network)
          (merge mobile-network-settings/settings-sheet)

          (= view :mobile-network-offline)
          (merge mobile-network-settings/offline-sheet)

          (= view :add-new)
          (merge home.sheet/add-new)

          (= view :new-chat-bottom-sheet)
          (merge home.sheet/new-chat-bottom-sheet-comp)

          (= view :start-a-new-chat)
          (merge home.sheet/start-a-new-chat)

          (= view :keycard.login/more)
          (merge keycard/more-sheet)

          (= view :learn-more)
          (merge about-app/learn-more)

          (= view :recover-sheet)
          (merge recover.views/bottom-sheet)

          (= view :migrate-account-password)
          (merge key-storage/migrate-account-password)

          (= view :pinned-messages-list)
          (merge {:content                   pinned-messages-menu/pinned-messages
                  :bottom-safe-area-spacing? false}))]
    [:f>
     (fn []
       (rn/use-effect (fn []
                        (rn/hw-back-add-listener dismiss-bottom-sheet-callback)
                        (fn []
                          (rn/hw-back-remove-listener dismiss-bottom-sheet-callback))))
       [bottom-sheet/bottom-sheet opts
        (when content
          [content (when options options)])])]))
