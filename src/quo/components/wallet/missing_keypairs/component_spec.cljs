(ns quo.components.wallet.missing-keypairs.component-spec
  (:require
    [quo.components.wallet.missing-keypairs.view :as missing-keypairs]
    [test-helpers.component :as h]))

(def ^:private theme :dark)

(def props
  {:blur?            true
   :container-style  {}
   :on-options-press (fn [])
   :keypairs         [{:type     :seed
                       :name     name
                       :key-uid  "123"
                       :accounts [{:customization-color :turquoise
                                   :emoji               "\uD83C\uDFB2"
                                   :type                :default}]}]})

(h/describe "Wallet: Missing key pairs"
  (h/test "Missing key pair title renders"
    (h/render-with-theme-provider [missing-keypairs/view props]
                                  theme)
    (h/is-truthy (h/get-by-label-text :title))
    (h/is-truthy (h/get-by-label-text :t/import-to-use-derived-accounts))))
