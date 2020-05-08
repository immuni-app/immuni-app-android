const lint = require('@commitlint/lint');
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