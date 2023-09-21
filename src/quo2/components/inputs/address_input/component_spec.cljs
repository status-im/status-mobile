(ns quo2.components.inputs.address-input.component-spec
  (:require [quo2.components.inputs.address-input.view :as address-input]
            [test-helpers.component :as h]
            [react-native.clipboard :as clipboard]
            [quo2.foundations.colors :as colors]))

(def ens-regex #"^(?=.{5,255}$)([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.[a-zA-Z]{2,}$")

(h/describe "Address input"
  (h/test "default render"
    (with-redefs [clipboard/get-string #(% "")]
      (h/render [address-input/address-input {:ens-regex ens-regex}])
      (h/is-truthy (h/get-by-label-text :address-text-input))))

  (h/test "on focus with blur? false"
    (with-redefs [clipboard/get-string #(% "")]
      (h/render [address-input/address-input {:ens-regex ens-regex}])
      (h/fire-event :on-focus (h/get-by-label-text :address-text-input))
      (h/has-prop (h/get-by-label-text :address-text-input) :placeholder-text-color colors/neutral-40)))

  (h/test "on focus with blur? true"
    (with-redefs [clipboard/get-string #(% "")]
      (h/render [address-input/address-input
                 {:blur?     true
                  :ens-regex ens-regex}])
      (h/fire-event :on-focus (h/get-by-label-text :address-text-input))
      (h/has-prop (h/get-by-label-text :address-text-input)
                  :placeholder-text-color
                  colors/neutral-80-opa-40)))

  (h/test "scanned value is properly set"
    (let [on-change-text (h/mock-fn)
          scanned-value  "scanned-value"]
      (with-redefs [clipboard/get-string #(% "")]
        (h/render [address-input/address-input
                   {:scanned-value  scanned-value
                    :on-change-text on-change-text
                    :ens-regex      ens-regex}])
        (h/wait-for #(h/is-truthy (h/get-by-label-text :clear-button)))
        (h/was-called-with on-change-text scanned-value)
        (h/has-prop (h/get-by-label-text :address-text-input) :default-value scanned-value))))

  (h/test "clear icon is shown when input has text"
    (with-redefs [clipboard/get-string #(% "")]
      (h/render [address-input/address-input
                 {:scanned-value "scanned value"
                  :ens-regex     ens-regex}])
      (h/wait-for #(h/is-truthy (h/get-by-label-text :clear-button-container)))
      (h/wait-for #(h/is-truthy (h/get-by-label-text :clear-button)))))

  (h/test "on blur with text and blur? false"
    (with-redefs [clipboard/get-string #(% "")]
      (h/render [address-input/address-input
                 {:scanned-value "scanned value"
                  :ens-regex     ens-regex}])
      (h/wait-for #(h/is-truthy (h/get-by-label-text :clear-button)))
      (h/fire-event :on-focus (h/get-by-label-text :address-text-input))
      (h/fire-event :on-blur (h/get-by-label-text :address-text-input))
      (h/has-prop (h/get-by-label-text :address-text-input) :placeholder-text-color colors/neutral-30)))

  (h/test "on blur with text blur? true"
    (with-redefs [clipboard/get-string #(% "")]
      (h/render [address-input/address-input
                 {:scanned-value "scanned value"
                  :blur?         true
                  :ens-regex     ens-regex}])
      (h/wait-for #(h/is-truthy (h/get-by-label-text :clear-button)))
      (h/fire-event :on-focus (h/get-by-label-text :address-text-input))
      (h/fire-event :on-blur (h/get-by-label-text :address-text-input))
      (h/has-prop (h/get-by-label-text :address-text-input)
                  :placeholder-text-color
                  colors/neutral-80-opa-20)))

  (h/test "on blur with no text and blur? false"
    (with-redefs [clipboard/get-string #(% "")]
      (h/render [address-input/address-input {:ens-regex ens-regex}])
      (h/fire-event :on-focus (h/get-by-label-text :address-text-input))
      (h/fire-event :on-blur (h/get-by-label-text :address-text-input))
      (h/has-prop (h/get-by-label-text :address-text-input) :placeholder-text-color colors/neutral-40)))

  (h/test "on blur with no text blur? true"
    (with-redefs [clipboard/get-string #(% "")]
      (h/render [address-input/address-input
                 {:blur?     true
                  :ens-regex ens-regex}])
      (h/fire-event :on-focus (h/get-by-label-text :address-text-input))
      (h/fire-event :on-blur (h/get-by-label-text :address-text-input))
      (h/has-prop (h/get-by-label-text :address-text-input)
                  :placeholder-text-color
                  colors/neutral-80-opa-40)))

  (h/test "on-clear is called"
    (let [on-clear (h/mock-fn)]
      (with-redefs [clipboard/get-string #(% "")]
        (h/render [address-input/address-input
                   {:scanned-value "scanned value"
                    :on-clear      on-clear
                    :ens-regex     ens-regex}])
        (h/wait-for #(h/is-truthy (h/get-by-label-text :clear-button)))
        (h/fire-event :press (h/get-by-label-text :clear-button))
        (h/was-called on-clear))))

  (h/test "on-focus is called"
    (let [on-focus (h/mock-fn)]
      (with-redefs [clipboard/get-string #(% "")]
        (h/render [address-input/address-input {:on-focus on-focus}])
        (h/fire-event :on-focus (h/get-by-label-text :address-text-input))
        (h/was-called on-focus))))

  (h/test "on-blur is called"
    (let [on-blur (h/mock-fn)]
      (with-redefs [clipboard/get-string #(% "")]
        (h/render [address-input/address-input
                   {:on-blur   on-blur
                    :ens-regex ens-regex}])
        (h/fire-event :on-blur (h/get-by-label-text :address-text-input))
        (h/was-called on-blur))))

  (h/test "on-scan is called"
    (let [on-scan (h/mock-fn)]
      (with-redefs [clipboard/get-string #(% "")]
        (h/render [address-input/address-input {:on-scan on-scan}])
        (h/wait-for #(h/is-truthy (h/get-by-label-text :scan-button)))
        (h/fire-event :press (h/get-by-label-text :scan-button))
        (h/was-called on-scan))))

  (h/test "paste from clipboard"
    (let [clipboard "clipboard"]
      (with-redefs [clipboard/get-string #(% clipboard)]
        (h/render [address-input/address-input {:ens-regex ens-regex}])
        (h/wait-for #(h/is-truthy (h/get-by-label-text :paste-button)))
        (h/fire-event :press (h/get-by-label-text :paste-button))
        (h/wait-for #(h/is-truthy (h/get-by-label-text :clear-button)))
        (h/has-prop (h/get-by-label-text :address-text-input) :default-value clipboard))))

  (h/test "ENS loading state and call on-detect-ens"
    (let [clipboard     "test.eth"
          on-detect-ens (h/mock-fn)]
      (with-redefs [clipboard/get-string #(% clipboard)]
        (h/render [address-input/address-input
                   {:on-detect-ens on-detect-ens
                    :ens-regex     ens-regex}])
        (h/wait-for #(h/is-truthy (h/get-by-label-text :paste-button)))
        (h/fire-event :press (h/get-by-label-text :paste-button))
        (h/wait-for #(h/is-falsy (h/get-by-label-text :clear-button)))
        (h/wait-for #(h/is-truthy (h/get-by-label-text :loading-button-container)))
        (h/was-called on-detect-ens))))

  (h/test "ENS valid state and call on-detect-ens"
    (let [clipboard     "test.eth"
          on-detect-ens (h/mock-fn)]
      (with-redefs [clipboard/get-string #(% clipboard)]
        (h/render [address-input/address-input
                   {:on-detect-ens on-detect-ens
                    :valid-ens?    true
                    :ens-regex     ens-regex}])
        (h/wait-for #(h/is-truthy (h/get-by-label-text :paste-button)))
        (h/fire-event :press (h/get-by-label-text :paste-button))
        (h/wait-for #(h/is-falsy (h/get-by-label-text :clear-button)))
        (h/wait-for #(h/is-truthy (h/get-by-label-text :positive-button-container)))
        (h/was-called on-detect-ens)))))
