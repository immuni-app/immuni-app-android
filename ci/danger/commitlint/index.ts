// index.ts
// Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
// Please refer to the AUTHORS file for more information.
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <https://www.gnu.org/licenses/>.

const lint = require("@commitlint/lint");
import { danger, fail, message } from "../danger";
import defaultRules from "./rules";

type LintError = {
  message: string;
  name: string;
  valid: boolean;
  level: number;
};
type CommitWithErrors = { message: string; sha: string; errors: LintError[] };

export default async ({
  enabled = true,
  allowedScopes = []
}: {
  enabled: boolean;
  allowedScopes: string[];
}): Promise<void> => {
  console.log("Starting Commitlint check...");

  if (!enabled) {
    console.log("Skipping Commitlint check because it is disabled.");
    return;
  }

  const allRules = {
    "scope-enum": [2, "always", allowedScopes],
    ...defaultRules
  };

  const wrongCommits: CommitWithErrors[] = [];

  try {
    for (const commit of danger.git.commits) {
      const result = await lint(commit.message, allRules);

      if (!result.valid) {
        wrongCommits.push({
          sha: commit.sha,
          message: commit.message,
          errors: result.errors
        });

        console.log("Result:", result);
      }
    }

    if (wrongCommits.length > 0) {
      throw new Error("Commitlint failed for some commits.");
    }

    console.log(`Commitlint passed.`);

    message(`:white_check_mark: Commitlint passed`);
  } catch (err) {
    const wrongMessages: string[] = wrongCommits.map(
      commit =>
        `[${commit.sha}] ${commit.message}\n\t- ${commit.errors
          .map(error => error.message)
          .join("\n\t- ")}`
    );

    console.log(`Commitlint failed:`, err);

    console.log(`Wrong commits:`, wrongMessages);

    fail(
      `Commitlint failed on the following commits (please do rebase): \n - ${wrongMessages.join(
        "\n - "
      )}\n\nLook at our rules and the [Conventional Commits](https://www.conventionalcommits.org) guidelines.`
    );
  }
};
