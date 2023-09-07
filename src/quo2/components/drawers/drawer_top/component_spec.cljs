(ns quo2.components.drawers.drawer-top.component-spec
  (:require [test-helpers.component :as h]
            [quo2.core :as quo]))

(h/describe "drawer top tests"
  (h/test "component renders in default type"
    (h/render [quo/drawer-top
               {:title "Title"
                :type  :default}])
    (h/is-truthy (h/get-by-text "Title")))

  (h/test "component renders in default + description type"
    (h/render [quo/drawer-top
               {:title       "Title"
                :type        :default
                :description "Description"}])
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-text "Description")))

  (h/test "component renders in info type"
    (h/render [quo/drawer-top
               {:title "Title"
                :type  :info}])
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-label-text :info-icon)))

  (h/test "component renders in info + description type"
    (h/render [quo/drawer-top
               {:title       "Title"
                :description "Description"
                :type        :info}])
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-text "Description"))
    (h/is-truthy (h/get-by-label-text :info-icon)))

  (h/test "component renders in context-tag type"
    (h/render [quo/drawer-top
               {:title          "Title"
                :type           :context-tag
                :community-name "Coinbase"}])
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-label-text :context-tag-wrapper)))

  (h/test "component renders in context-tag + button type"
    (h/render [quo/drawer-top
               {:title          "Title"
                :type           :context-tag
                :button-icon    :i/placeholder
                :community-name "Coinbase"}])
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-label-text :button-icon))
    (h/is-truthy (h/get-by-label-text :context-tag-wrapper)))

  (h/test "component renders in account type"
    (h/render [quo/drawer-top
               {:title                "Title"
                :type                 :account
                :account-avatar-emoji "🍿"
                :networks             [:ethereum]
                :description          "0x62b...0a5"
                :customization-color  :purple}])
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-text "0x62b...0a5"))
    (h/is-truthy (h/get-by-label-text :account-avatar)))

  (h/test "component renders in keypair type when keycard? is false"
    (h/render [quo/drawer-top
               {:title       "Title"
                :keycard?    false
                :icon-avatar :i/placeholder
                :type        :keypair}])
    (h/is-truthy (h/get-by-text "Title"))
    (-> (h/expect (h/get-by-translation-text :on-device))
        (.toBeTruthy)))

  (h/test "component renders in keypair type when keycard? is true"
    (h/render [quo/drawer-top
               {:title       "Title"
                :keycard?    true
                :icon-avatar :i/placeholder
                :type        :keypair}])
    (h/is-truthy (h/get-by-text "Title"))
    (-> (h/expect (h/get-by-translation-text :on-keycard))
        (.toBeTruthy)))

  (h/test "component renders in default-keypair type"
    (h/render [quo/drawer-top
               {:title       "Title"
                :description "0x62b...0a5"
                :type        :default-keypair}])
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-label-text :default-keypair-text)))

  (h/test "component renders in label type"
    (h/render [quo/drawer-top
               {:label "label"
                :type  :label}])
    (h/is-truthy (h/get-by-text "label"))))
