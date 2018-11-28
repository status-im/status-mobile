#include "desktopmenu.h"
#include "bridge.h"

#include <QCoreApplication>
#include <QDebug>
#include <QMenu>
#include <QCursor>

Q_LOGGING_CATEGORY(DESKTOPMENU, "DesktopMenu")

namespace {
struct RegisterQMLMetaType {
  RegisterQMLMetaType() { qRegisterMetaType<DesktopMenu *>(); }
} registerMetaType;
} // namespace

class DesktopMenuPrivate {
public:
  Bridge *bridge = nullptr;
  void createMenu(const QStringList& items, double callback);
private:
  void onTriggered(QAction* action);
};

void DesktopMenuPrivate::createMenu(const QStringList& items, double callback) {
  QMenu* menu = new QMenu();
  for (const QString& name : items) {
    menu->addAction(name);
  }
  QObject::connect(menu, &QMenu::triggered, [=](QAction* action) {
    bridge->invokePromiseCallback(callback, QVariantList{action->text()});
  });
  QObject::connect(menu, &QMenu::triggered, menu, &QMenu::deleteLater);
  menu->popup(QCursor::pos());
}

DesktopMenu::DesktopMenu(QObject *parent)
    : QObject(parent), d_ptr(new DesktopMenuPrivate) {
}

DesktopMenu::~DesktopMenu() {
}

void DesktopMenu::setBridge(Bridge *bridge) {
  Q_D(DesktopMenu);

  d->bridge = bridge;
}

QString DesktopMenu::moduleName() { return "DesktopMenuManager"; }

QList<ModuleMethod *> DesktopMenu::methodsToExport() {
  return QList<ModuleMethod *>{};
}

QVariantMap DesktopMenu::constantsToExport() { return QVariantMap(); }

void DesktopMenu::show(const QStringList& items, double callback) {
  Q_D(DesktopMenu);
  d_ptr->createMenu(items, callback);

}


