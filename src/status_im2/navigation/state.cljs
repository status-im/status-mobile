(ns status-im2.navigation.state)

(defonce root-id (atom nil))
(defonce pushed-screen-id (atom nil))
(defonce curr-modal (atom nil))
(defonce modals (atom []))
(defonce dissmissing (atom false))
