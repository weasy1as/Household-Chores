import MemberRow from "@/components/household/member-row";

type Member = {
  userId: string;
  displayName: string | null;
  role: string;
  status: string;
  rotationPosition: number;
};

type MemberListProps = {
  householdId: string;
  members: Member[];
};

export default function MemberList({ householdId, members }: MemberListProps) {
  return (
    <section>
      <h2>Members</h2>

      {members.map((member) => (
        <MemberRow
          key={member.userId}
          householdId={householdId}
          member={member}
        />
      ))}
    </section>
  );
}
