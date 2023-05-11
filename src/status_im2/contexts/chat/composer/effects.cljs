(ns status-im2.contexts.chat.composer.effects
  (:require
    [react-native.platform :as platform]
    [status-im.async-storage.core :as async-storage]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.composer.constants :as constants]
    [status-im2.contexts.chat.composer.keyboard :as kb]
    [utils.number :as utils.number]
    [oops.core :as oops]
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
  [{:keys [kb-default-height]}]
  (when (zero? @kb-default-height)
    (async-storage/get-item :kb-default-height
                            #(reset! kb-default-height (utils.number/parse-int % nil)))))

(defn background-effect
  [{:keys [maximized?]}
   {:keys [opacity background-y]}
   {:keys [max-height]}
   {:keys [input-content-height]}]
  (when (or @maximized? (>= input-content-height (* max-height constants/background-threshold)))
    (reanimated/set-shared-value background-y 0)
    (reanimated/animate opacity 1)))

(defn images-effect
  [{:keys [sending-images? input-ref]}
   {:keys [container-opacity]}
   images?]
  (when images?
    (reanimated/animate container-opacity 1))
  (when (and (not @sending-images?) images? @input-ref)
    (.focus ^js @input-ref)
    (reset! sending-images? true))
  (when-not images?
    (reset! sending-images? false)))

(defn audio-effect
  [{:keys [recording? gesture-enabled?]}
   {:keys [container-opacity]}
   audio]
  (when (and audio (not @recording?))
    (reset! recording? true)
    (reset! gesture-enabled? false)
    (reanimated/animate container-opacity 1)))

(defn empty-effect
  [{:keys [text-value maximized? focused?]}
   {:keys [container-opacity]}
   images?
   reply?
   audio]
  (when
    (and (empty? @text-value) (not images?) (not reply?) (not @maximized?) (not @focused?) (not audio))
    (reanimated/animate-delay container-opacity constants/empty-opacity 200)))

(defn component-will-unmount
  [{:keys [keyboard-show-listener keyboard-hide-listener keyboard-frame-listener]}]
  (.remove ^js @keyboard-show-listener)
  (.remove ^js @keyboard-hide-listener)
  (.remove ^js @keyboard-frame-listener))

(defn initialize
  [props state animations {:keys [max-height] :as dimensions} chat-input images? reply?
   audio]
  (rn/use-effect
   (fn []
     (maximized-effect state animations dimensions chat-input)
     (reenter-screen-effect state dimensions chat-input)
     (layout-effect state)
     (kb-default-height-effect state)
     (background-effect state animations dimensions chat-input)
     (images-effect props animations images?)
     (audio-effect state animations audio)
     (empty-effect state animations images? reply? audio)
     (kb/add-kb-listeners props state animations dimensions)
     #(component-will-unmount props))
   [max-height]))

(defn edit
  [{:keys [input-ref]}
   {:keys [text-value saved-cursor-position]}
   edit]
  (rn/use-effect
   (fn []
     (let [edit-text (get-in edit [:content :text])]
       (when (and edit @input-ref)
         (.focus ^js @input-ref)
         (.setNativeProps ^js @input-ref (clj->js {:text edit-text}))
         (reset! text-value edit-text)
         (reset! saved-cursor-position (count edit-text)))))
   [(:message-id edit)]))

(defn reply
  [{:keys [input-ref]}
   {:keys [container-opacity]}
   reply]
  (rn/use-effect
   (fn []
     (when reply
       (reanimated/animate container-opacity 1))
     (when (and reply @input-ref)
       (.focus ^js @input-ref)))
   [(:message-id reply)]))

(defn edit-mentions
  [{:keys [input-ref]} {:keys [text-value cursor-position]} input-with-mentions]
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
   input-text]
  (rn/use-effect
   (fn []
     (when (and input-text (not= @text-value input-text))
       (when @input-ref
         (.setNativeProps ^js @input-ref (clj->js {:text input-text})))
       (reset! text-value input-text)
       (rf/dispatch [:mention/on-change-text input-text])))
   [input-text]))

(defn did-mount
  [{:keys [selectable-input-ref input-ref selection-manager]} state]
  (rn/use-effect
   (fn []
     (when platform/android?
       (let [selectable-text-input-handle (rn/find-node-handle @selectable-input-ref)
             text-input-handle            (rn/find-node-handle @input-ref)]
         (oops/ocall selection-manager
                     :setupMenuItems
                     selectable-text-input-handle
                     text-input-handle))))))
