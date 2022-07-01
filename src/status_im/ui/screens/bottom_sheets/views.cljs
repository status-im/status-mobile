(ns status-im.ui.screens.bottom-sheets.views
  (:require [status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.home.sheet.views :as home.sheet]
            [status-im.ui.screens.keycard.views :as keycard]
            [status-im.ui.screens.multiaccounts.key-storage.views :as key-storage]
            [status-im.ui.screens.about-app.views :as about-app]
            [status-im.ui.screens.communities.sort-communities-redesign :as sort-communities-redesign]
            [status-im.ui.screens.multiaccounts.recover.views :as recover.views]
            [quo.components.bottom-sheet.view :as bottom-sheet]
            [quo.core :as quo]))

(defn bottom-sheet []
  (let [{:keys [show? view options]} @(re-frame/subscribe [:bottom-sheet])
        {:keys [content]
         :as   opts}
        (cond-> {:visible?  show?
                 :on-cancel #(re-frame/dispatch [:bottom-sheet/hide])}

          (map? view)
          (merge view)

          (= view :mobile-network)
          (merge mobile-network-settings/settings-sheet)

          (= view :mobile-network-offline)
          (merge mobile-network-settings/offline-sheet)

          (= view :add-new)
          (merge home.sheet/add-new)

          (= view :keycard.login/more)
          (merge keycard/more-sheet)

          (= view :learn-more)
          (merge about-app/learn-more)

          (= view :recover-sheet)
          (merge recover.views/bottom-sheet)

          (= view :migrate-account-password)
          (merge key-storage/migrate-account-password)

          (= view :sort-communities)
          (merge sort-communities-redesign/sort-communities))]
    [quo/bottom-sheet opts
     (when content
       [content (when options options)])]))