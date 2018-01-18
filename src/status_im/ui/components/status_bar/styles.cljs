(ns status-im.ui.components.status-bar.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.styles :refer [defstyle]]))

(def elevation 2)

(defn- create-status-bar-style [{:keys [background-color bar-style translucent?]
                                 :or   {bar-style "light-content"}}]
  {:background-color (if translucent? "transparent" background-color)
   :translucent      translucent?
   :bar-style        bar-style})

(defn- create-view-style [{:keys [background-color height elevation]
                           :or   {height (get platform/platform-specific :status-bar-default-height)}}]
  {:background-color background-color
   :elevation        elevation
   :height           height})

;; :main
(defstyle status-bar-main
  {:ios     (create-status-bar-style {:background-color colors/white
                                      :bar-style        "default"})
   :android (create-status-bar-style {:translucent?     true
                                      :bar-style        "dark-content"})})

(def view-main
  (create-view-style {:background-color colors/white}))

;; :transparent
(defstyle status-bar-transparent
  {:ios     (create-status-bar-style {:background-color styles/color-transparent})
   :android (create-status-bar-style {:translucent?     true})})

(def view-transparent
  (create-view-style {:background-color styles/color-transparent}))

;; :modal
(defstyle status-bar-modal
  {:ios     (create-status-bar-style {:background-color "#2f3031"})
   :android (create-status-bar-style {:background-color styles/color-black})})

(defstyle view-modal
  {:ios     (create-view-style {:background-color "#2f3031"})
   :android (create-view-style {:background-color styles/color-black
                                :height           0})})

;; :modal-white
(defstyle status-bar-modal-white
  {:ios     (create-status-bar-style {:background-color colors/white
                                      :bar-style        "default"})
   :android (create-status-bar-style {:background-color styles/color-black
                                      :bar-style        "light-content"})})

(defstyle view-modal-white
  {:ios     (create-view-style {:background-color colors/white})
   :android (create-view-style {:background-color styles/color-black
                                :height           0})})

;; :modal-wallet
(def status-bar-modal-wallet
  (create-status-bar-style {:background-color colors/blue}))

(defstyle view-modal-wallet
  {:ios     (create-view-style {:background-color colors/blue})
   :android (create-view-style {:background-color colors/blue
                                :height           0
                                :elevation        elevation})})

;; :transaction
(defstyle status-bar-transaction
  {:ios     (create-status-bar-style {:background-color styles/color-transparent})
   :android (create-status-bar-style {:background-color styles/color-dark-blue-2})})

(defstyle view-transaction
  {:ios     (create-view-style {:background-color styles/color-transparent})
   :android (create-view-style {:background-color styles/color-dark-blue-2
                                :height           0})})

;; TODO(jeluard) Fix status-bar mess by removing useless view and introducing 2dn level tab-bar

;; :wallet HOME
(defstyle status-bar-wallet-tab
  {:ios     (create-status-bar-style {:background-color colors/blue})
   :android (create-status-bar-style {:translucent?     true})})

(def view-wallet-tab
  (create-view-style {:background-color styles/color-blue4}))

;; :wallet
(defstyle status-bar-wallet
          {:ios     (create-status-bar-style {:background-color colors/blue})
           :android (create-status-bar-style {:translucent?     true})})

(def view-wallet
  (create-view-style {:background-color styles/color-blue4
                      :elevation        elevation}))

;; :default
(defstyle status-bar-default
  {:ios     (create-status-bar-style {:background-color colors/white
                                      :bar-style        "default"})
   :android (create-status-bar-style {:translucent?     true
                                      :bar-style        "dark-content"})})

(defstyle view-default
  (create-view-style {:background-color colors/white
                      :elevation        elevation}))
