(ns status-im.contexts.chat.messenger.composer.utils
  (:require
    [status-im.contexts.chat.messenger.composer.constants :as constants]
    [utils.number]
    [utils.re-frame :as rf]))

(defn calc-top-content-height
  [reply? edit?]
  (cond-> 0
    reply? (+ constants/reply-container-height)
    edit?  (+ constants/edit-container-height)))

(defn calc-bottom-content-height
  [images link-previews?]
  (cond-> 0
    (seq images)   (+ constants/images-container-height)
    link-previews? (+ constants/links-container-height)))

(defn blur-input
  [input-ref]
  (when @input-ref
    (rf/dispatch [:chat.ui/set-input-focused false])
    (.blur ^js @input-ref)))

(defn cancel-reply-message
  [input-ref]
  (js/setTimeout #(blur-input input-ref) 100)
  (rf/dispatch [:chat.ui/cancel-message-reply]))

(defn cancel-edit-message
  [input-ref]
  ;; NOTE: adding a timeout to assure the input is blurred on the next tick
  ;; after the `text-value` was cleared. Otherwise the height will be calculated
  ;; with the old `text-value`, leading to wrong composer height after blur.
  (js/setTimeout
   (fn []
     (blur-input input-ref))
   100)
  (.setNativeProps ^js @input-ref (clj->js {:text ""}))
  (rf/dispatch [:chat.ui/cancel-message-edit]))

(defn calc-suggestions-position
  [cursor-pos max-height size
   {:keys [maximized?]}
   {:keys [insets curr-height window-height keyboard-height reply edit]}
   images
   link-previews?]
  (let [base             (+ constants/composer-default-height (:bottom insets) 8)
        base             (+ base (- curr-height constants/input-height))
        base             (+ base (calc-top-content-height reply edit))
        view-height      (- window-height keyboard-height (:top insets))
        container-height (utils.number/value-in-range
                          (* (/ constants/mentions-max-height 4) size)
                          (/ constants/mentions-max-height 4)
                          constants/mentions-max-height)]
    (if @maximized?
      (if (< (+ cursor-pos container-height) max-height)
        (+ constants/actions-container-height (:bottom insets))
        (+ constants/actions-container-height (:bottom insets) (- max-height cursor-pos) 18))
      (if (< (+ base container-height) view-height)
        (let [bottom-content-height (calc-bottom-content-height images link-previews?)]
          (+ base bottom-content-height))
        (+ constants/actions-container-height (:bottom insets) (- curr-height cursor-pos) 18)))))
