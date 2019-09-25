(ns status-im.ui.components.status-bar.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]
            [status-im.utils.styles :as styles]))

(defn- create-status-bar-style [{:keys [background-color bar-style translucent?]
                                 :or   {bar-style "light-content"}}]
  {:background-color (if translucent? "transparent" background-color)
   :translucent      translucent?
   :bar-style        bar-style})

(defn- create-view-style
  [{:keys [background-color height]
    :or   {height (get platform/platform-specific :status-bar-default-height)}}]
  {:background-color background-color
   :height           height})

;; :main
(styles/def status-bar-main
  {:ios     (create-status-bar-style {:background-color colors/white
                                      :bar-style        "default"})
   :android (create-status-bar-style {:translucent?     true
                                      :bar-style        "dark-content"})})

(styles/def status-bar-main-main
  {:ios     (create-status-bar-style {:background-color colors/white
                                      :bar-style        "default"})
   :android (create-status-bar-style {:background-color colors/black
                                      :bar-style        "light-content"})})

(def view-main
  (create-view-style {:background-color colors/white}))

(styles/def view-modal-main
  {:ios     (create-view-style {:background-color colors/white})
   :android (create-view-style {:background-color colors/black
                                :height           0})})

;; :transparent
(styles/def status-bar-transparent
  {:ios     (create-status-bar-style {:background-color colors/transparent})
   :android (create-status-bar-style {:translucent?     true})})

(def view-transparent
  (create-view-style {:background-color colors/transparent}))

;; :modal
(styles/def status-bar-modal
  {:ios     (create-status-bar-style {:background-color "#2f3031"})
   :android (create-status-bar-style {:background-color colors/black})})

(styles/def view-modal
  {:ios     (create-view-style {:background-color "#2f3031"})
   :android (create-view-style {:background-color colors/black
                                :height           0})})

;; :modal-white
(styles/def status-bar-modal-white
  {:ios     (create-status-bar-style {:background-color colors/white
                                      :bar-style        "default"})
   :android (create-status-bar-style {:background-color colors/black
                                      :bar-style        "light-content"})})

(styles/def view-modal-white
  {:ios     (create-view-style {:background-color colors/white})
   :android (create-view-style {:background-color colors/black
                                :height           0})})

;; :modal-wallet
(def status-bar-modal-wallet
  (create-status-bar-style {:background-color colors/blue}))

(styles/def view-modal-wallet
  {:ios     (create-view-style {:background-color colors/blue})
   :android (create-view-style {:background-color colors/blue
                                :height           0})})

;; :transaction
(styles/def status-bar-transaction
  {:ios     (create-status-bar-style {:background-color colors/transparent})
   :android (create-status-bar-style {:background-color colors/black})})

(styles/def view-transaction
  {:ios     (create-view-style {:background-color colors/transparent})
   :android (create-view-style {:background-color colors/black
                                :height           0})})

;; TODO(jeluard) Fix status-bar mess by removing useless view and introducing 2dn level tab-bar

;; :wallet HOME
(styles/def status-bar-wallet-tab
  {:ios     (create-status-bar-style {:background-color colors/blue})
   :android (create-status-bar-style {:translucent?     true})})

(def view-wallet-tab
  (create-view-style {:background-color colors/blue}))

;; :wallet
(styles/def status-bar-wallet
  {:ios     (create-status-bar-style {:background-color colors/blue})
   :android (create-status-bar-style {:translucent?     true})})

(def view-wallet
  (create-view-style {:background-color colors/blue}))

;; :default
(styles/def status-bar-default
  {:ios     (create-status-bar-style {:background-color colors/white
                                      :bar-style        "default"})
   :android (create-status-bar-style {:translucent?     true
                                      :bar-style        "dark-content"})})

(def view-default
  (create-view-style {}))
