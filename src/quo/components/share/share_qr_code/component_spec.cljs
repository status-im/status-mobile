(ns quo.components.share.share-qr-code.component-spec
  (:require [quo.components.share.share-qr-code.view :as share-qr-code]
            [test-helpers.component :as h]))

(defn render-share-qr-code
  [{share-qr-type :type :as props}]
  (let [component-rendered (h/render [share-qr-code/view {:type share-qr-type}])
        rerender-fn        (h/get-rerender-fn component-rendered)
        share-qr-code      (h/get-by-label-text :share-qr-code)]
    ;; Fires on-layout since it's needed to render the content
    (h/fire-event :layout share-qr-code #js {:nativeEvent #js {:layout #js {:width 500}}})
    (rerender-fn [share-qr-code/view props])))

(h/describe "Share QR Code component"
  (h/describe "Renders share-qr-code component in all types"
    (let [qr-label "Text shown below QR"]
      (h/test "Profile"
        (render-share-qr-code {:type    :profile
                               :qr-data qr-label})
        (h/is-truthy (h/get-by-text qr-label)))

      (h/test "Wallet Legacy"
        (render-share-qr-code {:type    :wallet-legacy
                               :qr-data qr-label
                               :emoji   "👻"})
        (h/is-truthy (h/get-by-text qr-label)))

      (h/test "Wallet Multichain"
        (render-share-qr-code {:type    :wallet-multichain
                               :qr-data qr-label
                               :emoji   "👻"})
        (h/is-truthy (h/get-by-text qr-label)))))

  (h/describe "Fires all events for all types"
    (letfn [(test-fire-events [props test-seq]
              (doseq [{:keys [test-name event-name
                              callback-prop-key
                              accessibility-label]} test-seq
                      :let                          [event-fn (h/mock-fn)]]
                (h/test test-name
                  (render-share-qr-code (assoc props callback-prop-key event-fn))
                  (h/fire-event event-name (h/get-by-label-text accessibility-label))
                  (h/was-called-times event-fn 1))))]

      (h/describe "Profile"
        (test-fire-events
         {:type :profile}
         [{:test-name           "Text pressed"
           :accessibility-label :share-qr-code-info-text
           :event-name          :press
           :callback-prop-key   :on-text-press}
          {:test-name           "Text long pressed"
           :accessibility-label :share-qr-code-info-text
           :event-name          :long-press
           :callback-prop-key   :on-text-long-press}
          {:test-name           "Share button"
           :accessibility-label :link-to-profile
           :event-name          :press
           :callback-prop-key   :on-share-press}]))

      (h/describe "Wallet Legacy"
        (test-fire-events
         {:type :wallet-legacy :emoji "👽"}
         [{:test-name           "Text pressed"
           :accessibility-label :share-qr-code-info-text
           :event-name          :press
           :callback-prop-key   :on-text-press}
          {:test-name           "Text long pressed"
           :accessibility-label :share-qr-code-info-text
           :event-name          :long-press
           :callback-prop-key   :on-text-long-press}
          {:test-name           "Share button pressed"
           :accessibility-label :link-to-profile
           :event-name          :press
           :callback-prop-key   :on-share-press}
          {:test-name           "Info icon pressed"
           :accessibility-label :share-qr-code-info-icon
           :event-name          :press
           :callback-prop-key   :on-info-press}
          {:test-name           "Legacy tab pressed"
           :accessibility-label :share-qr-code-legacy-tab
           :event-name          :press
           :callback-prop-key   :on-legacy-press}
          {:test-name           "Multichain tab pressed"
           :accessibility-label :share-qr-code-multichain-tab
           :event-name          :press
           :callback-prop-key   :on-multichain-press}]))

      (h/describe "Wallet Multichain"
        (test-fire-events
         {:type :wallet-multichain :emoji "👽"}
         [{:test-name           "Text pressed"
           :accessibility-label :share-qr-code-info-text
           :event-name          :press
           :callback-prop-key   :on-text-press}
          {:test-name           "Text long pressed"
           :accessibility-label :share-qr-code-info-text
           :event-name          :long-press
           :callback-prop-key   :on-text-long-press}
          {:test-name           "Share button pressed"
           :accessibility-label :link-to-profile
           :event-name          :press
           :callback-prop-key   :on-share-press}
          {:test-name           "Info icon pressed"
           :accessibility-label :share-qr-code-info-icon
           :event-name          :press
           :callback-prop-key   :on-info-press}
          {:test-name           "Legacy tab pressed"
           :accessibility-label :share-qr-code-legacy-tab
           :event-name          :press
           :callback-prop-key   :on-legacy-press}
          {:test-name           "Multichain tab pressed"
           :accessibility-label :share-qr-code-multichain-tab
           :event-name          :press
           :callback-prop-key   :on-multichain-press}
          {:test-name           "Settings pressed"
           :accessibility-label :share-qr-code-settings
           :event-name          :press
           :callback-prop-key   :on-settings-press}])))))
