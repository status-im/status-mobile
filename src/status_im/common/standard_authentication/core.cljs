(ns status-im.common.standard-authentication.core
  (:require
    status-im.common.standard-authentication.password-input.view
    status-im.common.standard-authentication.slide-auth
    status-im.common.standard-authentication.slide-sign))

(def password-input status-im.common.standard-authentication.password-input.view/view)
(def slide-auth status-im.common.standard-authentication.slide-auth/view)
(def slide-sign status-im.common.standard-authentication.slide-sign/view)
