(ns status-im.ui.screens.bottom-sheets.views
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.about-app.views :as about-app]
            [status-im.ui.screens.home.sheet.views :as home.sheet]
            [status-im.ui.screens.keycard.views :as keycard]
            [status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
            [status-im.ui.screens.multiaccounts.key-storage.views :as key-storage]
            [status-im.ui.screens.multiaccounts.recover.views :as recover.views]
            [status-im2.common.bottom-sheet.view :as bottom-sheet]
            [status-im2.contexts.chat.messages.pin.list.view :as pin.list]
            [reagent.core :as reagent]
            [status-im2.contexts.chat.messages.drawers.view :as drawers]))

(defn bottom-sheet
  []
  (let [{:keys [show? view options]} @(re-frame/subscribe [:bottom-sheet])
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
          (merge {:content pin.list/pinned-messages-list})

          (= view :drawer/reactions)
          (merge {:content drawers/reactions}))]
    (reagent/create-class {:reagent-render         (fn []
                                                     [bottom-sheet/bottom-sheet
                                                      (case view
                                                        :new-chat-bottom-sheet
                                                        (assoc opts :initial-height 150)
                                                        :drawer/reactions
                                                        (assoc opts :initial-height 100)
                                                        opts)
                                                      (when content
                                                        [content (when options options)])])
                           :component-will-unmount (fn []
                                                     (when @bottom-sheet/show-bottom-sheet?
                                                       (bottom-sheet/reset-atoms)))})))
