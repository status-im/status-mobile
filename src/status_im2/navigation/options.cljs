(ns status-im2.navigation.options
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(defn default-options
  []
  {:layout {:orientation :portrait}
   :topBar {:visible false}})

(defn statusbar-and-navbar-root
  []
  (if platform/android?
    {:navigationBar {:backgroundColor colors/neutral-100}
     :statusBar     {:translucent     true
                     :backgroundColor :transparent
                     :style           :light
                     :drawBehind      true}}
    {:statusBar {:style :light}}))

(defn default-root
  []
  (merge (statusbar-and-navbar-root)
         {:topBar {:visible false}
          :layout {:componentBackgroundColor (colors/theme-colors colors/white colors/neutral-100)
                   :orientation              :portrait
                   :backgroundColor          (colors/theme-colors colors/white colors/neutral-100)}}))

(defn navbar
  []
  {:navigationBar {:backgroundColor (colors/theme-colors colors/white colors/neutral-100)}})

(defn statusbar
  []
  (if platform/android?
    {:statusBar {:translucent     true
                 :backgroundColor :transparent
                 :drawBehind      true
                 :style           (if (colors/dark?) :light :dark)}}
    {:statusBar {:style (if (colors/dark?) :light :dark)}}))


(defn statusbar-and-navbar
  []
  (merge (navbar) (statusbar)))

(defn topbar-options
  []
  {:noBorder             true
   :scrollEdgeAppearance {:active   false
                          :noBorder true}
   :elevation            0
   :title                {:color (colors/theme-colors colors/neutral-100 colors/white)}
   :rightButtonColor     (colors/theme-colors colors/neutral-100 colors/white)
   :background           {:color (colors/theme-colors colors/white colors/neutral-100)}
   :backButton           {:color  (colors/theme-colors colors/neutral-100 colors/white)
                          :testID :back-button}})

(def transparent-screen-options
  (merge
   {:modalPresentationStyle :overCurrentContext
    :layout                 {:componentBackgroundColor :transparent
                             :orientation              :portrait
                             :backgroundColor          :transparent}}
   (if platform/android?
     {:statusBar {:backgroundColor :transparent
                  :style           :light
                  :drawBehind      true}}
     {:statusBar {:style :light}})))

(def sheet-options
  {:layout                 {:componentBackgroundColor :transparent
                            :backgroundColor          :transparent}
   :modalPresentationStyle :overCurrentContext})

(def lightbox
  {:topBar        {:visible false}
   :statusBar     {:backgroundColor :transparent
                   :style           :light
                   :animate         true
                   :drawBehind      true
                   :translucent     true}
   :navigationBar {:backgroundColor colors/black}
   :layout        {:componentBackgroundColor :transparent
                   :backgroundColor          :transparent}
   :animations    {:push {:sharedElementTransitions [{:fromId        :shared-element
                                                      :toId          :shared-element
                                                      :interpolation {:type   :decelerate
                                                                      :factor 1.5}}]}
                   :pop  {:sharedElementTransitions [{:fromId        :shared-element
                                                      :toId          :shared-element
                                                      :interpolation {:type
                                                                      :decelerate
                                                                      :factor 1.5}}]}}})

(defn merge-top-bar
  [root-options options]
  (let [options (:topBar options)]
    {:topBar
     (merge root-options
            options
            (when (or (:title root-options) (:title options))
              {:title (merge (:title root-options) (:title options))})
            (when (or (:background root-options) (:background options))
              {:background (merge (:background root-options) (:background options))})
            (when (or (:backButton root-options) (:backButton options))
              {:backButton (merge (:backButton root-options) (:backButton options))})
            (when (or (:leftButtons root-options) (:leftButtons options))
              {:leftButtons (merge (:leftButtons root-options) (:leftButtons options))})
            (when (or (:rightButtons root-options) (:rightButtons options))
              {:rightButtons (merge (:rightButtons root-options) (:rightButtons options))}))}))
