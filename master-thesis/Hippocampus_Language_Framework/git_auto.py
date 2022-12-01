import socket
import subprocess


def git(folder_name: str) -> None:
    # git-Commit des Modells
    print(f"\nGIT\n\t{folder_name}\n")

    # Nur im CIP wird direkt gepusht, weil dort das Prozedere umständlicher ist
    if "cip" in socket.gethostname():
        commit_message = "\"[CIP AUTO] Training erfolgreich => Modell verfügbar\""
        git_push = f"git push origin main"

        # Code, falls ich verhindern möchte, dass zuviele Commits den Log fluten und "--amend" ausgeführt werden soll
        # last_commit_message = subprocess.run(git_last_commit_message.split(" "),
        #                                      stdout=subprocess.PIPE).stdout.decode("utf-8")
        # git_last_commit_message = "git log -1 --pretty=%B"
        # git_force_push = f"git push -f origin {branch}"
        # subprocess.run(["git", "add", f"Saved_Models/{folder_name}/"])  # Nicht als Liste spezifizierbar, da zu viele Leerzeichen enthalten wären und man dann zu viele Elemente für "run" hätte
        #
        # # Wenn letzte Commit-Message mit aktueller übereinstimmt, wird "--amend" aufgerufen, damit der Log nicht mit automatischen Commits geflutet wird
        # if last_commit_message == commit_message:
        #     git_commit_amend = "git commit --amend"
        #     subprocess.run(git_commit_amend.split(" "))
        #     subprocess.run(git_force_push.split(" "))
        # else:
        #     subprocess.run(["git", "commit", "-m", commit_message])
        #     subprocess.run(git_push.split(" "))

        print(f"GIT ADD\n\t{folder_name}")
        subprocess.run(["git", "add", folder_name])  # Nicht als Liste spezifizierbar, da zu viele Leerzeichen enthalten wären und man dann zu viele Elemente für "run" hätte
        # Es wird erst gepusht, nachdem der Plot hinzugefügt wurde
        if "plots" in folder_name:
            print("GIT COMMIT")
            subprocess.run(["git", "commit", "-m", commit_message])
            print("GIT PUSH")
            subprocess.run(git_push.split(" "))
    # Auf meinem Rechner werden die Modell-Dateien nur hinzugefügt
    else:
        if "Saved_Models" in folder_name:
            # Damit mehrere Trainingsdurchläufe nicht die Commit-Übersicht/Ausgabe von
            # "git status"/Staging-Bereich mit Modelldateien fluten,
            # wird dieser (der Staging-Bereich) erst von den alten Modelldateien befreit
            git_restore_old_model_files = "git restore --staged *.json *.h5"
            subprocess.run(git_restore_old_model_files.split(" "))
            # Hinzufügen des aktuellen trainierten Modells
            print(f"GIT ADD\n\t{folder_name}")
            subprocess.run(["git", "add", folder_name])  # Nicht als Liste spezifizierbar, da zu viele Leerzeichen enthalten wären und man dann zu viele Elemente für "run" hätte
        if "plots" in folder_name:
            print(f"GIT ADD\n\t{folder_name}")
            subprocess.run(["git", "add", folder_name])


if __name__ == "__main__":
    pass
