(ns status-im2.contexts.chat.composer.effects
  (:require
    [clojure.string :as string]
    [oops.core :as oops]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.async-storage :as async-storage]
    [status-im2.contexts.chat.composer.constants :as constants]
    [status-im2.contexts.chat.composer.keyboard :as kb]
    [status-im2.contexts.chat.composer.utils :as utils]
    [utils.number]
    [utils.re-frame :as rf]))

(defn reenter-screen-effect
  [{:keys [text-value saved-cursor-position maximized?]}
   {:keys [content-height]}
   {:keys [input-content-height input-text input-maximized?]}]
  (when (and (empty? @text-value) (not= input-text nil))
    (reset! text-value input-text)
    (reset! content-height input-content-height)
    (reset! saved-cursor-position (count input-text)))
  (when input-maximized?
    (reset! maximized? true)))

(defn maximized-effect
  [{:keys [maximized?]}
   {:keys [height saved-height last-height]}
   {:keys [max-height]}
   {:keys [input-content-height]}]
  (when (or @maximized? (>= input-content-height max-height))
    (reanimated/animate height max-height)
    (reanimated/set-shared-value saved-height max-height)
    (reanimated/set-shared-value last-height max-height)))

(defn layout-effect
  [{:keys [lock-layout?]}]
  (when-not @lock-layout?
    (js/setTimeout #(reset! lock-layout? true) 500)))

(defn kb-default-height-effect
  [{:keys [kb-default-height kb-height]}]
  (when (zero? @kb-default-height)
    (async-storage/get-item :kb-default-height
                            (fn [height]
                              (reset! kb-default-height (utils.number/parse-int height 0))
                              (reset! kb-height (utils.number/parse-int height 0))))))

(defn background-effect
  [{:keys [maximized?]}
   {:keys [opacity background-y]}
   {:keys [max-height]}
   {:keys [input-content-height]}]
  (when (or @maximized? (>= input-content-height (* max-height constants/background-threshold)))
    (reanimated/set-shared-value background-y 0)
    (reanimated/animate opacity 1)))

(defn link-preview-effect
  [{:keys [text-value]}]
  (let [text @text-value]
    (when-not (string/blank? text)
      (rf/dispatch [:link-preview/unfurl-urls text]))))

(defn audio-effect
  [{:keys [recording? gesture-enabled?]}
   {:keys [container-opacity]}
   audio]
  (when (and audio (not @recording?))
    (reset! recording? true)
    (reset! gesture-enabled? false)
    (reanimated/animate container-opacity 1)))

(defn empty-effect
  [{:keys [focused?]}
   {:keys [container-opacity]}
   {:keys [input-text images link-previews? reply audio]}]
  (when (and (not @focused?) (utils/empty-input? input-text images link-previews? reply audio))
    (reanimated/animate-delay container-opacity constants/empty-opacity 200)))

(defn component-will-unmount
  [{:keys [keyboard-show-listener keyboard-hide-listener keyboard-frame-listener]}]
  (.remove ^js @keyboard-show-listener)
  (.remove ^js @keyboard-hide-listener)
  (.remove ^js @keyboard-frame-listener))

(defn initialize
  [props state animations {:keys [max-height] :as dimensions}
   {:keys [chat-input audio] :as subscriptions}]
  (rn/use-effect
   (fn []
     (maximized-effect state animations dimensions chat-input)
     (layout-effect state)
     (kb-default-height-effect state)
     (background-effect state animations dimensions chat-input)
     (link-preview-effect state)
     (audio-effect state animations audio)
     (empty-effect state animations subscriptions)
     (kb/add-kb-listeners props state animations dimensions)
     #(component-will-unmount props))
   [max-height])
  (rn/use-effect
   (fn []
     (reenter-screen-effect state dimensions subscriptions))
   [max-height subscriptions]))

(defn use-edit
  [{:keys [input-ref]}
   {:keys [text-value saved-cursor-position]}
   {:keys [edit]}]
  (rn/use-effect
   (fn []
     (let [edit-text        (get-in edit [:content :text])
           text-value-count (count @text-value)]
       (when (and edit @input-ref)
         (.focus ^js @input-ref)
         (.setNativeProps ^js @input-ref (clj->js {:text edit-text}))
         (reset! text-value edit-text)
         (reset! saved-cursor-position (if (zero? text-value-count)
                                         (count edit-text)
                                         text-value-count)))))
   [(:message-id edit)]))

(defn use-reply
  [{:keys [input-ref]}
   {:keys [container-opacity]}
   {:keys [reply]}]
  (rn/use-effect
   (fn []
     (when reply
       (reanimated/animate container-opacity 1))
     (when (and reply @input-ref)
       (.focus ^js @input-ref)))
   [(:message-id reply)]))

(defn edit-mentions
  [{:keys [input-ref]} {:keys [text-value cursor-position]} {:keys [input-with-mentions]}]
  (rn/use-effect (fn []
                   (let [input-text (reduce (fn [acc item]
                                              (str acc (second item)))
                                            ""
                                            input-with-mentions)]
                     (reset! text-value input-text)
                     (reset! cursor-position (count input-text))
                     (js/setTimeout #(when @input-ref
                                       (.setNativeProps ^js @input-ref
                                                        (clj->js {:selection {:start (count input-text)
                                                                              :end   (count
                                                                                      input-text)}})))
                                    300)))
                 [(some #(= :mention (first %)) (seq input-with-mentions))]))

(defn update-input-mention
  [{:keys [input-ref]}
   {:keys [text-value]}
   {:keys [input-text]}]
  (rn/use-effect
   (fn []
     (when (and input-text (not= @text-value input-text))
       (when @input-ref
         (.setNativeProps ^js @input-ref (clj->js {:text input-text})))
       (reset! text-value input-text)
       (rf/dispatch [:mention/on-change-text input-text])))
   [input-text]))

(defn link-previews
  [{:keys [sending-links?]}
   {:keys [text-value maximized?]}
   {:keys [height saved-height last-height]}
   {:keys [link-previews?]}]
  (rn/use-effect
   (fn []
     (if-not @maximized?
       (let [value (if link-previews?
                     constants/links-container-height
                     (- constants/links-container-height))]
         (when (not= @sending-links? link-previews?)
           (reanimated/animate height (+ (reanimated/get-shared-value saved-height) value))
           (reanimated/set-shared-value saved-height
                                        (+ (reanimated/get-shared-value saved-height) value))
           (reanimated/set-shared-value last-height
                                        (+ (reanimated/get-shared-value last-height) value))))
       (let [curr-text @text-value]
         (reset! text-value (str @text-value " "))
         (js/setTimeout #(reset! text-value curr-text) 10)))
     (reset! sending-links? link-previews?))
   [link-previews?]))

(defn use-images
  [{:keys [sending-images? input-ref]}
   {:keys [text-value maximized?]}
   {:keys [container-opacity height saved-height last-height]}
   {:keys [images]}]
  (rn/use-effect
   (fn []
     (when images
       (reanimated/animate container-opacity 1))
     (when (and (not @sending-images?) (seq images) @input-ref)
       (.focus ^js @input-ref))
     (if-not @maximized?
       (let [value (if (seq images)
                     constants/images-container-height
                     (- constants/images-container-height))]
         (when (not= @sending-images? (boolean (seq images)))
           (reanimated/animate height (+ (reanimated/get-shared-value saved-height) value))
           (reanimated/set-shared-value saved-height
                                        (+ (reanimated/get-shared-value saved-height) value))
           (reanimated/set-shared-value last-height
                                        (+ (reanimated/get-shared-value last-height) value))))
       (let [curr-text @text-value]
         (reset! text-value (str @text-value " "))
         (js/setTimeout #(reset! text-value curr-text) 10)))
     (reset! sending-images? (boolean (seq images))))
   [(boolean (seq images))]))

(defn did-mount
  [{:keys [selectable-input-ref input-ref selection-manager]}]
  (rn/use-effect
   (fn []
     (when platform/android?
       (let [selectable-text-input-handle (rn/find-node-handle @selectable-input-ref)
             text-input-handle            (rn/find-node-handle @input-ref)]
         (oops/ocall selection-manager
                     :setupMenuItems
                     selectable-text-input-handle
                     text-input-handle))))))
