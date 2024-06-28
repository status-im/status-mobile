(ns quo.components.wallet.keypair.component-spec
  (:require
    [quo.components.wallet.keypair.view :as keypair]
    [test-helpers.component :as h]))

(def ^:private theme :light)

(def accounts
  [{:account-props {:customization-color :turquoise
                    :size                32
                    :emoji               "\uD83C\uDFB2"
                    :type                :default
                    :name                "Trip to Vegas"
                    :address             "0x0ah...71a"}
    :networks      [{:network-name :ethereum :short-name "eth"}
                    {:network-name :optimism :short-name "oeth"}]
    :action        :none}])

(def default-details
  {:full-name "John Doe"
   :address   "zQ3...6fBd2"})

(def other-details {:full-name "Metamask"})

(h/describe "Wallet: Keypair"
  (h/test "Default keypair title renders"
    (h/render-with-theme-provider [keypair/view
                                   {:accounts            accounts
                                    :customization-color :blue
                                    :type                :default-keypair
                                    :stored              :on-device
                                    :action              :selector
                                    :details             default-details}]
                                  theme)
    (h/is-truthy (h/get-by-label-text :title)))

  (h/test "On device renders"
    (h/render-with-theme-provider [keypair/view
                                   {:accounts            accounts
                                    :customization-color :blue
                                    :type                :other
                                    :stored              :on-device
                                    :action              :selector
                                    :details             other-details}]
                                  theme)
    (h/is-truthy (h/get-by-label-text :details)))

  (h/test "Selector action renders"
    (h/render-with-theme-provider [keypair/view
                                   {:accounts            accounts
                                    :customization-color :blue
                                    :type                :other
                                    :stored              :on-keycard
                                    :action              :selector
                                    :details             other-details}]
                                  theme)
    (h/is-truthy (h/get-by-label-text :radio-off)))

  (h/test "Options action renders"
    (h/render-with-theme-provider [keypair/view
                                   {:accounts            accounts
                                    :customization-color :blue
                                    :type                :other
                                    :stored              :on-keycard
                                    :action              :options
                                    :details             other-details}]
                                  theme)
    (h/is-truthy (h/get-by-label-text :options-button))))
