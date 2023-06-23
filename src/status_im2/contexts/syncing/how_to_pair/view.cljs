(ns status-im2.contexts.syncing.how-to-pair.view
  (:require [status-im2.contexts.syncing.syncing-instructions.view :as syncing-instructions]))

(defn view
  []
  [syncing-instructions/instructions
   {:title-label-key :t/how-to-pair
    :mobile          [{:title :t/signing-in-from-another-device
                       :image {:source :mobile-how-to-pair-sign-in
                               :type   :image}
                       :list  [[[:text :t/open-status-on-your-other-device]]
                               [[:text :t/tap-on]
                                [:button-grey :t/im-new]]
                               [[:button-primary :t/enable-camera]
                                [:text :t/or-tap]
                                [:button-grey :t/enter-sync-code]]
                               [[:text :t/scan-or-enter-sync-code-seen-on-this-device]]]}
                      {:title :t/already-logged-in-on-the-other-device
                       :image {:source :mobile-how-to-pair-logged-in
                               :type   :image}
                       :list  [[[:text :t/tap-on]
                                [:context-tag
                                 {:label  :t/profile
                                  :source :user-picture-male5}]
                                [:text :t/and]
                                [:button-grey-placeholder :t/syncing]]
                               [[:text :t/press]
                                [:button-grey-placeholder :t/scan-or-enter-sync-code]]
                               [[:button-primary :t/enable-camera]
                                [:text :t/or-tap]
                                [:button-grey :t/enter-sync-code]]
                               [[:text :t/scan-or-enter-sync-code-seen-on-this-device]]]}]
    :desktop         [{:title :t/signing-in-from-another-device
                       :image {:source :desktop-how-to-pair-sign-in
                               :type   :image}
                       :list  [[[:text :t/open-status-on-your-other-device]]
                               [[:text :t/open]
                                [:button-grey-placeholder :t/settings]
                                [:text :t/and-go-to]
                                [:button-grey-placeholder :t/syncing]]
                               [[:text :t/tap]
                                [:button-grey :t/scan-or-enter-a-sync-code]]
                               [[:text :t/scan-the-qr-code-or-copy-the-sync-code]]]}
                      {:title :t/already-logged-in-on-the-other-device
                       :image {:source :desktop-how-to-pair-logged-in
                               :type   :container-image}
                       :list  [[[:text :t/open-status-on-your-other-device]]
                               [[:text :t/tap-on]
                                [:button-grey :t/im-new]]
                               [[:button-primary :t/enable-camera]
                                [:text :t/or-tap]
                                [:button-grey :t/enter-sync-code]]
                               [[:text :t/scan-or-enter-sync-code-seen-on-this-device]]]}]}])
