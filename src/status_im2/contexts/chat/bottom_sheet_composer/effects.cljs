(ns status-im2.contexts.chat.bottom-sheet-composer.effects
  (:require
    [react-native.platform :as platform]
    [status-im.async-storage.core :as async-storage]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.bottom-sheet-composer.constants :as constants]
    [status-im2.contexts.chat.bottom-sheet-composer.keyboard :as kb]
    [utils.number :as utils.number]
    [oops.core :as oops]))

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
  (when-not @kb-default-height
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

(defn images-or-reply-effect
  [{:keys [container-opacity]}
   {:keys [replying? sending-images? input-ref]}
   images? reply?]
  (when (or images? reply?)
    (reanimated/animate container-opacity 1))
  (when (and (not @sending-images?) images? @input-ref)
    (.focus ^js @input-ref)
    (reset! sending-images? true))
  (when (and (not @replying?) reply? @input-ref)
    (.focus ^js @input-ref)
    (reset! replying? true))
  (when-not images?
    (reset! sending-images? false))
  (when-not reply?
    (reset! replying? false)))

(defn edit-effect
  [{:keys [text-value saved-cursor-position]}
   {:keys [editing? input-ref]}
   edit]
  (let [edit-text (get-in edit [:content :text])]
    (when (and (not @editing?) edit @input-ref)
      (.focus ^js @input-ref)
      (reset! editing? true)
      (reset! text-value edit-text)
      (reset! saved-cursor-position (count edit-text)))
    (when-not edit-text
      (reset! editing? false))))

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
  [props state animations {:keys [max-height] :as dimensions} chat-input keyboard-height images? reply?
   edit audio]
  (rn/use-effect
   (fn []
     (maximized-effect state animations dimensions chat-input)
     (reenter-screen-effect state dimensions chat-input)
     (layout-effect state)
     (kb-default-height-effect state)
     (background-effect state animations dimensions chat-input)
     (images-or-reply-effect animations props images? reply?)
     (edit-effect state props edit)
     (audio-effect state animations audio)
     (empty-effect state animations images? reply? audio)
     (kb/add-kb-listeners props state animations dimensions keyboard-height)
     #(component-will-unmount props))
   [max-height]))

(defn setup-selection
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
