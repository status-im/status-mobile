(ns status-im2.contexts.shell.animation
  (:require [quo2.foundations.colors :as colors]
            [utils.re-frame :as rf]
            [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]
            [status-im.async-storage.core :as async-storage] ;;TODO remove when not used anymore
            [status-im2.contexts.shell.constants :as shell.constants]
            [worklets.shell]))

;; Atoms
(def selected-stack-id (atom nil))
(def screen-height (atom nil))
(def home-stack-state (atom shell.constants/close-with-animation))
(def shared-values-atom (atom nil))

;; Reagent atoms used for lazily loading home screen tabs
(def load-communities-stack? (reagent/atom false))
(def load-chats-stack? (reagent/atom false))
(def load-wallet-stack? (reagent/atom false))
(def load-browser-stack? (reagent/atom false))

;; Helper Functions
(defn home-stack-open?
  []
  (let [state @home-stack-state]
    (or (= state shell.constants/open-with-animation)
        (= state shell.constants/open-without-animation))))

(defn load-stack
  [stack-id]
  (case stack-id
    :communities-stack (reset! load-communities-stack? true)
    :chats-stack       (reset! load-chats-stack? true)
    :wallet-stack      (reset! load-wallet-stack? true)
    :browser-stack     (reset! load-browser-stack? true)
    ""))

(defn selected-stack-id-loaded
  [stack-id]
  (reset! selected-stack-id stack-id)
  (reset!
    home-stack-state
    (if (some? stack-id)
      shell.constants/open-with-animation
      shell.constants/close-with-animation)))

(defn calculate-home-stack-position
  []
  (let [{:keys [width height]} (shell.constants/dimensions)
        height                 (or @screen-height height)
        bottom-nav-tab-width   90
        minimize-scale         (/ bottom-nav-tab-width width)
        empty-space-half-scale (/ (- 1 minimize-scale) 2)
        left-margin            (/ (- width (* 4 bottom-nav-tab-width)) 2)
        left-empty-space       (* empty-space-half-scale width)
        top-empty-space        (* empty-space-half-scale
                                  (- height (shell.constants/bottom-tabs-container-height)))]
    {:left  (reduce
             (fn [acc stack-id]
               (assoc acc
                      stack-id
                      (+ (- left-margin left-empty-space)
                         (* (.indexOf shell.constants/stacks-ids stack-id)
                            bottom-nav-tab-width))))
             {:none 0}
             shell.constants/stacks-ids)
     :top   (+ top-empty-space (shell.constants/bottom-tabs-container-height))
     :scale minimize-scale}))

;; Shared Values
(defn calculate-shared-values
  []
  (let [selected-stack-id-sv    (reanimated/use-shared-value
                                 ;; passing keywords or nil is not working with reanimated
                                 (name (or @selected-stack-id :communities-stack)))
        pass-through-sv         (reanimated/use-shared-value false)
        home-stack-state-sv     (reanimated/use-shared-value @home-stack-state)
        animate-home-stack-left (reanimated/use-shared-value (not (home-stack-open?)))
        home-stack-position     (calculate-home-stack-position)]
    (reset! shared-values-atom
      (reduce
       (fn [acc id]
         (let [tabs-icon-color-keyword (get shell.constants/tabs-icon-color-keywords id)
               stack-opacity-keyword   (get shell.constants/stacks-opacity-keywords id)
               stack-pointer-keyword   (get shell.constants/stacks-pointer-keywords id)]
           (assoc
            acc
            stack-opacity-keyword
            (worklets.shell/stack-opacity (name id) selected-stack-id-sv)
            stack-pointer-keyword
            (worklets.shell/stack-pointer (name id) selected-stack-id-sv)
            tabs-icon-color-keyword
            (worklets.shell/bottom-tab-icon-color
             (name id)
             selected-stack-id-sv
             home-stack-state-sv
             pass-through-sv
             colors/white
             colors/neutral-50
             colors/white-opa-40))))
       {:selected-stack-id       selected-stack-id-sv
        :pass-through?           pass-through-sv
        :home-stack-state        home-stack-state-sv
        :animate-home-stack-left animate-home-stack-left
        :home-stack-left         (worklets.shell/home-stack-left
                                  selected-stack-id-sv
                                  animate-home-stack-left
                                  home-stack-state-sv
                                  (clj->js (:left home-stack-position)))
        :home-stack-top          (worklets.shell/home-stack-top
                                  home-stack-state-sv
                                  (:top home-stack-position))
        :home-stack-opacity      (worklets.shell/home-stack-opacity home-stack-state-sv)
        :home-stack-pointer      (worklets.shell/home-stack-pointer home-stack-state-sv)
        :home-stack-scale        (worklets.shell/home-stack-scale home-stack-state-sv
                                                                  (:scale home-stack-position))
        :bottom-tabs-height      (worklets.shell/bottom-tabs-height
                                  home-stack-state-sv
                                  (shell.constants/bottom-tabs-container-height)
                                  (shell.constants/bottom-tabs-extended-container-height))}
       shell.constants/stacks-ids)))
  @shared-values-atom)

;; Animations

(defn change-root-status-bar-style
  []
  (rf/dispatch [:change-root-status-bar-style
                (if (or (colors/dark?)
                        (not (home-stack-open?)))
                  :light
                  :dark)]))

(defn open-home-stack
  [stack-id animate?]
  (let [home-stack-state-value (if animate?
                                 shell.constants/open-with-animation
                                 shell.constants/open-without-animation)]
    (reanimated/set-shared-value
     (:selected-stack-id @shared-values-atom)
     (name stack-id))
    (reanimated/set-shared-value
     (:home-stack-state @shared-values-atom)
     home-stack-state-value)
    (reset! home-stack-state home-stack-state-value)
    (js/setTimeout
     change-root-status-bar-style
     shell.constants/shell-animation-time)
    (reset! selected-stack-id stack-id)
    (async-storage/set-item! :selected-stack-id stack-id)))

(defn change-tab
  [stack-id]
  (reanimated/set-shared-value (:animate-home-stack-left @shared-values-atom) false)
  (reanimated/set-shared-value (:selected-stack-id @shared-values-atom) (name stack-id))
  (reset! selected-stack-id stack-id)
  (async-storage/set-item! :selected-stack-id stack-id))

(defn bottom-tab-on-press
  [stack-id]
  (when (and @shared-values-atom (not= stack-id @selected-stack-id))
    (let [stack-load-delay (if (home-stack-open?)
                             0
                             shell.constants/shell-animation-time)]
      (if (home-stack-open?)
        (change-tab stack-id)
        (open-home-stack stack-id true))
      (js/setTimeout #(load-stack stack-id) stack-load-delay))))

(defn close-home-stack
  [animate?]
  (let [home-stack-state-value (if animate?
                                 shell.constants/close-with-animation
                                 shell.constants/close-without-animation)]
    (reanimated/set-shared-value
     (:animate-home-stack-left @shared-values-atom)
     true)
    (reanimated/set-shared-value
     (:home-stack-state @shared-values-atom)
     home-stack-state-value)
    (reset! home-stack-state home-stack-state-value)
    (change-root-status-bar-style)
    (reset! selected-stack-id nil)
    (async-storage/set-item! :selected-stack-id nil)))
