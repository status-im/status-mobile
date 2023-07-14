(ns quo2.components.share.share-qr-code.component-spec
  (:require [quo2.components.share.share-qr-code.view :as share-qr-code]
            [test-helpers.component :as h]))

(h/describe "Share QR Code component"
  (h/test "renders share qr code component"
    (h/render [share-qr-code/view
               {:link-title " A test title"}])
    (-> (js/expect (h/get-by-text "A test title"))
        (.toBeTruthy)))

  (h/test "renders share qr code url"
    (h/render [share-qr-code/view
               {:qr-url " A test url"}])
    (-> (js/expect (h/get-by-text "A test url"))
        (.toBeTruthy)))

  (h/test "on press link event fires"
    (let [event (h/mock-fn)]
      (h/render [share-qr-code/view
                 {:url-on-press event
                  :qr-url       " A test url"}])
      (h/fire-event :press (h/get-by-text "A test url"))
      (-> (js/expect event)
          (.toHaveBeenCalledTimes 1))))

  (h/test "on press share event fires"
    (let [event (h/mock-fn)]
      (h/render [share-qr-code/view
                 {:share-on-press event}])
      (h/fire-event :press (h/get-by-label-text :share-profile))
      (-> (js/expect event)
          (.toHaveBeenCalledTimes 1)))))
