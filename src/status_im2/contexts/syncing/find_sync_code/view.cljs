(ns status-im2.contexts.syncing.find-sync-code.view
  (:require [status-im2.contexts.syncing.syncing-instructions.view :as syncing-instructions]))

(defn view
  []
  [syncing-instructions/instructions
   {:title-label-key :t/find-sync-code
    :mobile          [{:image {:source :find-sync-code-mobile
                               :type   :image}
                       :list  [[[:text :t/open-status-on-your-other-device]]
                               [[:text :t/open-your]
                                [:context-tag
                                 {:label  :t/profile
                                  :source :user-picture-male5}]]
                               [[:text :t/go-to]
                                [:button-grey-placeholder :t/syncing]]
                               [[:text :t/tap]
                                [:button-grey :t/sync-new-device]
                                [:text :t/and]
                                [:button-primary :t/set-up-sync]]
                               [[:text :t/scan-the-qr-code-or-copy-the-sync-code]]]}]
    :desktop         [{:image {:source :find-sync-code-desktop
                               :type   :image}
                       :list  [[[:text :t/open-status-on-your-other-device]]
                               [[:text :t/open]
                                [:button-grey-placeholder :t/settings]
                                [:text :t/and-go-to]
                                [:button-grey-placeholder :t/syncing]]
                               [[:text :t/tap]
                                [:button-primary :t/set-up-sync]]
                               [[:text :t/scan-the-qr-code-or-copy-the-sync-code]]]}]}])
