(ns quo.components.drawers.drawer-top.component-spec
  (:require
    [quo.core :as quo]
    [test-helpers.component :as h]))

(def ^:private theme :light)

(h/describe "drawer top tests"
  (h/test "component renders in default type"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title "Title"
                                    :type  :default}]
                                  theme)
    (h/is-truthy (h/get-by-text "Title")))

  (h/test "component renders in default + description type"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title       "Title"
                                    :type        :default
                                    :description "Description"}]
                                  theme)
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-text "Description")))

  (h/test "component renders in address type"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title "0x1"
                                    :type  :address}]
                                  theme)
    (h/is-truthy (h/get-by-text "0x1")))

  (h/test "component renders in info type"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title "Title"
                                    :type  :info}]
                                  theme)
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-label-text :info-icon)))

  (h/test "component renders in info + description type"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title       "Title"
                                    :description "Description"
                                    :type        :info}]
                                  theme)
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-text "Description"))
    (h/is-truthy (h/get-by-label-text :info-icon)))

  (h/test "component renders in context-tag type"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title          "Title"
                                    :type           :context-tag
                                    :community-name "Coinbase"}]
                                  theme)
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-label-text :context-tag-wrapper)))

  (h/test "component renders in context-tag + button type"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title          "Title"
                                    :type           :context-tag
                                    :button-icon    :i/placeholder
                                    :community-name "Coinbase"}]
                                  theme)
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-label-text :button-icon))
    (h/is-truthy (h/get-by-label-text :context-tag-wrapper)))

  (h/test "component renders in account type"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title                "Title"
                                    :type                 :account
                                    :account-avatar-emoji "🍿"
                                    :networks             [{:network-name :ethereum :short-name "eth"}]
                                    :description          "0x62b...0a5"
                                    :customization-color  :purple}]
                                  theme)
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-text "0x62b...0a5"))
    (h/is-truthy (h/get-by-label-text :account-avatar)))

  (h/test "component renders keypair type with default label"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title       "Title"
                                    :stored      nil
                                    :icon-avatar :i/placeholder
                                    :type        :keypair}]
                                  theme)
    (h/is-truthy (h/get-by-text "Title"))
    (-> (h/expect (h/get-by-translation-text :t/on-device))
        (.toBeTruthy)))

  (h/test "component renders keypair type when stored on device"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title       "Title"
                                    :stored      :on-device
                                    :icon-avatar :i/placeholder
                                    :type        :keypair}]
                                  theme)
    (h/is-truthy (h/get-by-text "Title"))
    (-> (h/expect (h/get-by-translation-text :t/on-device))
        (.toBeTruthy)))

  (h/test "component renders keypair type when stored on keycard"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title       "Title"
                                    :stored      :on-keycard
                                    :icon-avatar :i/placeholder
                                    :type        :keypair}]
                                  theme)
    (h/is-truthy (h/get-by-text "Title"))
    (-> (h/expect (h/get-by-translation-text :t/on-keycard))
        (.toBeTruthy)))

  (h/test "component renders keypair type when considered missing"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title       "Title"
                                    :stored      :missing
                                    :icon-avatar :i/placeholder
                                    :type        :keypair}]
                                  theme)
    (h/is-truthy (h/get-by-text "Title"))
    (-> (h/expect (h/get-by-translation-text :t/import-to-use-derived-accounts))
        (.toBeTruthy)))

  (h/test "component renders in default-keypair type"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:title       "Title"
                                    :description "0x62b...0a5"
                                    :type        :default-keypair}]
                                  theme)
    (h/is-truthy (h/get-by-text "Title"))
    (h/is-truthy (h/get-by-label-text :default-keypair-text)))

  (h/test "component renders in label type"
    (h/render-with-theme-provider [quo/drawer-top
                                   {:label "label"
                                    :type  :label}]
                                  theme)
    (h/is-truthy (h/get-by-text "label"))))
