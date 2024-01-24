(ns quo.components.share.share-qr-code.component-spec
  (:require [quo.components.share.share-qr-code.view :as share-qr-code]
            [test-helpers.component :as h]))

(def qr-label "Text shown below QR")

(def default-props
  {:qr-data      qr-label
   :qr-image-uri "test-uri"
   :full-name    "Default name"})

(def profile-props
  {:type            :profile
   :profile-picture 123})

(def wallet-props {:networks '(:ethereum)})

(def text-press-event
  {:test-name           "Text pressed"
   :accessibility-label :share-qr-code-info-text
   :event-name          :press
   :callback-prop-key   :on-text-press})

(def text-long-press-event
  {:test-name           "Text long pressed"
   :accessibility-label :share-qr-code-info-text
   :event-name          :long-press
   :callback-prop-key   :on-text-long-press})

(def share-press-event
  {:test-name           "Share button"
   :accessibility-label :link-to-profile
   :event-name          :press
   :callback-prop-key   :on-share-press})

(def multichain-press-event
  {:test-name           "Multichain tab pressed"
   :accessibility-label :share-qr-code-multichain-tab
   :event-name          :press
   :callback-prop-key   :on-multichain-press})

(def legacy-press-event
  {:test-name           "Legacy tab pressed"
   :accessibility-label :share-qr-code-legacy-tab
   :event-name          :press
   :callback-prop-key   :on-legacy-press})

(def settings-press-event
  {:test-name           "Settings pressed"
   :accessibility-label :share-qr-code-settings
   :event-name          :press
   :callback-prop-key   :on-settings-press})

(defn render-share-qr-code
  [props]
  ;; NOTE: rendering the profile type first, so that the re-render is triggered.
  ;; Passing a :key breaks it for some reason.
  (let [component-rendered (h/render [share-qr-code/view
                                      (merge
                                       default-props
                                       profile-props
                                       {:qr-data "initial-render"})])
        rerender-fn        (h/get-rerender-fn component-rendered)
        share-qr-code      (h/get-by-label-text :share-qr-code)]
    ;; Fires on-layout since it's needed to render the content
    (h/fire-event :layout share-qr-code #js {:nativeEvent #js {:layout #js {:width 500}}})
    (rerender-fn [share-qr-code/view props])))

(h/describe "Share QR Code component"
  (h/describe "Renders share-qr-code component in all types"
    (h/test "Profile"
      (render-share-qr-code (merge default-props
                                   profile-props))
      (h/is-truthy (h/get-by-text qr-label)))

    (h/test "Wallet Legacy"
      (render-share-qr-code (merge default-props
                                   wallet-props
                                   {:type    :wallet
                                    :address :legacy}))
      (h/is-truthy (h/get-by-text qr-label)))

    (h/test "Wallet multichain"
      (render-share-qr-code (merge default-props
                                   wallet-props
                                   {:type    :wallet
                                    :address :multichain}))
      (h/is-truthy (h/get-by-text qr-label)))

    (h/test "Saved address legacy"
      (render-share-qr-code (merge default-props
                                   wallet-props
                                   {:type    :saved-address
                                    :address :legacy}))
      (h/is-truthy (h/get-by-text qr-label)))

    (h/test "Saved address multichain"
      (render-share-qr-code (merge default-props
                                   wallet-props
                                   {:type    :saved-address
                                    :address :multichain}))
      (h/is-truthy (h/get-by-text qr-label)))

    (h/test "Watched address legacy"
      (render-share-qr-code (merge default-props
                                   wallet-props
                                   {:type    :watched-address
                                    :address :legacy}))
      (h/is-truthy (h/get-by-text qr-label)))

    (h/test "Watched address multichain"
      (render-share-qr-code (merge default-props
                                   wallet-props
                                   {:type    :watched-address
                                    :address :multichain}))
      (h/is-truthy (h/get-by-text qr-label))))

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
         (merge default-props
                profile-props)
         [text-press-event
          text-long-press-event
          share-press-event]))

      (h/describe "Wallet Legacy"
        (test-fire-events
         (merge default-props
                wallet-props
                {:type    :wallet
                 :address :legacy
                 :emoji   "👻"})
         [text-press-event
          text-long-press-event
          share-press-event
          multichain-press-event
          legacy-press-event]))

      (h/describe "Wallet Multichain"
        (test-fire-events
         (merge default-props
                wallet-props
                {:type    :wallet
                 :address :multichain
                 :emoji   "👻"})
         [text-press-event
          text-long-press-event
          share-press-event
          legacy-press-event
          multichain-press-event
          settings-press-event]))

      (h/describe "Saved Address Legacy"
        (test-fire-events
         (merge default-props
                wallet-props
                {:type    :saved-address
                 :address :legacy})
         [text-press-event
          text-long-press-event
          share-press-event
          legacy-press-event
          multichain-press-event]))

      (h/describe "Saved Address Multichain"
        (test-fire-events
         (merge default-props
                wallet-props
                {:type :saved-address :address :multichain})
         [text-press-event
          text-long-press-event
          share-press-event
          legacy-press-event
          multichain-press-event
          settings-press-event]))

      (h/describe "Watched Address Legacy"
        (test-fire-events
         (merge default-props
                wallet-props
                {:type    :watched-address
                 :address :legacy
                 :emoji   "👽"})
         [text-press-event
          text-long-press-event
          share-press-event
          legacy-press-event
          multichain-press-event]))

      (h/describe "Watched Address Multichain"
        (test-fire-events
         (merge default-props
                wallet-props
                {:type    :watched-address
                 :address :multichain
                 :emoji   "👽"})
         [text-press-event
          text-long-press-event
          share-press-event
          legacy-press-event
          multichain-press-event
          settings-press-event])))))
